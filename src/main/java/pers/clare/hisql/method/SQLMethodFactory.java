package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.constant.CommandType;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.*;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.util.ArgumentParseUtil;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.ExceptionUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class SQLMethodFactory {
    private static final Map<Class<?>, Object> primitiveTypeNullDefaultMap = new ConcurrentHashMap<>();

    static {
        primitiveTypeNullDefaultMap.put(int.class, 0);
        primitiveTypeNullDefaultMap.put(long.class, 0L);
        primitiveTypeNullDefaultMap.put(float.class, 0f);
        primitiveTypeNullDefaultMap.put(double.class, 0d);
        primitiveTypeNullDefaultMap.put(boolean.class, false);
    }

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            Class<?> repositoryInterface
            , SQLStoreService sqlStoreService
    ) {
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        buildSqlInvoke(repositoryInterface, sqlStoreService, methodInterceptors);
        return methodInterceptors;
    }

    private static void buildSqlInvoke(
            Class<?> clazz
            , SQLStoreService sqlStoreService
            , Map<Method, MethodInterceptor> methodInterceptors
    ) {
        if (clazz == null
            || clazz == SQLRepository.class
            || clazz == SQLCrudRepository.class
        ) return;
        Class<?>[] superInterfaces = clazz.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            buildSqlInvoke(superInterface, sqlStoreService, methodInterceptors);
        }
        Method[] methods = ClassUtil.getDeclaredMethods(clazz);
        Map<String, String> commandMap = SQLInjector.getContents(sqlStoreService.getXmlRoot(), clazz);
        for (Method method : methods) {
            if (methodInterceptors.containsKey(method)) continue;
            int modifier = method.getModifiers();
            if (Modifier.isStatic(modifier) || !Modifier.isPublic(modifier)) continue;

            HiSql hiSql = method.getAnnotation(HiSql.class);
            String command = null;
            boolean autoKey = false;
            if (hiSql != null) {
                command = hiSql.value();
                autoKey = hiSql.returnIncrementKey();
                if (!StringUtils.hasLength(command)) {
                    command = commandMap.get(hiSql.name());
                }
            }
            if (!StringUtils.hasLength(command)) {
                command = commandMap.get(method.getName());
            }
            command = clearCommand(command);

            if (!StringUtils.hasLength(command)) {
                throw ExceptionUtil.insertAfter(method, new HiSqlException(String.format("%s.%s method must set XML or Sql.query", clazz.getName(), method.getName())));
            }
            methodInterceptors.put(method, buildInvoke(sqlStoreService, method, command, autoKey));
        }
    }

    private static MethodInterceptor buildInvoke(
            SQLStoreService sqlStoreService
            , Method method
            , String command
            , boolean autoKey
    ) {
        ArgumentParseUtil.ParseResult parseResult = ArgumentParseUtil.build(method);
        Function<Object[], String> sqlProcessor = buildSqlProcessor(
                command
                , parseResult.getGetters()
        );

        int commandType = sqlStoreService.getCommandTypeParser().parse(command);
        boolean optional = false;
        SqlInvoke sqlInvoke;
        if (parseResult.hasCallback()) {
            sqlInvoke = buildCallbackSqlInvoke(
                    commandType
                    , parseResult.getConnection()
                    , parseResult.getPreparedStatement()
                    , parseResult.getResultSet()
            );
        } else {
            Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                optional = ((ParameterizedType) type).getRawType() == Optional.class;
            } else {
                optional = type == Optional.class;
            }
            if (optional) {
                type = getValueType(type, 0);
            }

            switch (commandType) {
                case CommandType.Query:
                    sqlInvoke = buildSqlSelectInvoke(
                            type
                            , sqlStoreService.getNaming()
                            , sqlStoreService.getResultSetConverter()
                            , parseResult.getPagination()
                            , parseResult.getSort()
                    );
                    break;
                case CommandType.Insert:
                    sqlInvoke = buildSqlInsertInvoke(type, autoKey);
                    break;
                default:
                    sqlInvoke = buildSqlUpdateInvoke(type);
            }
        }

        if (sqlInvoke == null) {
            throw new HiSqlException("%s not support return type.", method.getName());
        }

        if (sqlProcessor == null) {
            if (optional) {
                return (invocation) -> Optional.ofNullable(sqlInvoke.apply(sqlStoreService, command, invocation.getArguments(), invocation.getArguments()));
            } else {
                return (invocation) -> sqlInvoke.apply(sqlStoreService, command, invocation.getArguments(), invocation.getArguments());
            }
        } else {
            if (optional) {
                return (invocation) -> Optional.ofNullable(sqlInvoke.apply(sqlStoreService, sqlProcessor.apply(invocation.getArguments()), null, invocation.getArguments()));
            } else {
                return (invocation) -> sqlInvoke.apply(sqlStoreService, sqlProcessor.apply(invocation.getArguments()), null, invocation.getArguments());
            }
        }
    }

    private static SqlInvoke buildCallbackSqlInvoke(
            int commandType
            , ArgumentHandler<ConnectionCallback<?>> connectionCallback
            , ArgumentHandler<PreparedStatementCallback<?>> preparedStatementCallback
            , ArgumentHandler<ResultSetCallback<?>> resultSetCallback
    ) {
        if (connectionCallback != null) {
            return (service, sql, arguments, originArguments) -> service.connection(sql, arguments, connectionCallback.apply(originArguments));
        }

        if (preparedStatementCallback != null) {
            return (service, sql, arguments, originArguments) -> service.prepared(sql, preparedStatementCallback.apply(originArguments));
        }

        if (resultSetCallback != null) {
            switch (commandType) {
                case CommandType.Query:
                    return (service, sql, arguments, originArguments) -> service.query(sql, arguments, resultSetCallback.apply(originArguments));
                case CommandType.Insert:
                    return (service, sql, arguments, originArguments) -> service.insert(sql, arguments);
                default:
                    return (service, sql, arguments, originArguments) -> service.update(sql, arguments);
            }
        }
        return null;
    }


    /**
     * build method interceptor by command type
     */
    private static SqlInvoke buildSqlSelectInvoke(
            Type type
            , NamingStrategy naming
            , ResultSetConverter converter
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.toClassType(type);
        if (Collection.class.isAssignableFrom(returnClass)) {
            if (returnClass == Set.class) {
                return buildSet(type, naming, converter, sortHandler);
            } else {
                return buildList(type, naming, converter, sortHandler);
            }
        } else if (returnClass.isArray()) {
            return buildList(type, naming, converter, sortHandler);
        } else if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else if (Page.class.isAssignableFrom(returnClass)) {
            return buildPage(type, naming, converter, paginationHandler, sortHandler);
        } else if (Next.class.isAssignableFrom(returnClass)) {
            return buildNext(type, naming, converter, paginationHandler, sortHandler);
        } else {
            if (returnClass.isPrimitive()) {
                final Class<?> objectClass = ClassUtil.toClassType(returnClass);
                return (service, sql, arguments, originArguments) -> {
                    Object result = service.find(objectClass, sql, applySort(sortHandler, originArguments), arguments);
                    return Objects.requireNonNullElse(result, primitiveTypeNullDefaultMap.get(returnClass));
                };
            } else if (SQLStoreFactory.isIgnore(returnClass)) {
                final Class<?> objectClass = ClassUtil.toClassType(returnClass);
                return (service, sql, arguments, originArguments) -> service.find(objectClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(naming, converter, returnClass);
                return (service, sql, arguments, originArguments) -> service.find(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    private static SqlInvoke buildSqlInsertInvoke(
            Type type
            , boolean autoKey
    ) {
        Class<?> keyClass = ClassUtil.toClassType(type);
        if (autoKey) {
            return (service, sql, arguments, originArguments) -> service.insert(keyClass, sql, arguments);
        } else {
            if (keyClass == int.class
                || keyClass == Integer.class
                || keyClass == void.class
            ) {
                return (service, sql, arguments, originArguments) -> service.insert(sql, arguments);
            } else if (keyClass == long.class
                       || keyClass == Long.class
            ) {
                return (service, sql, arguments, originArguments) -> service.insertLarge(sql, arguments);
            } else {
                throw new HiSqlException("Unsupported type : %s", keyClass);
            }
        }
    }

    /**
     * build method interceptor by command type
     */
    private static SqlInvoke buildSqlUpdateInvoke(
            Type type
    ) {
        Class<?> returnClass = ClassUtil.toClassType(type);
        if (returnClass == int.class
            || returnClass == Integer.class
            || returnClass == void.class
        ) {
            return (service, sql, arguments, originArguments) -> service.update(sql, arguments);
        } else if (returnClass == long.class
                   || returnClass == Long.class
        ) {
            return (service, sql, arguments, originArguments) -> service.updateLarge(sql, arguments);
        } else {
            throw new HiSqlException("Unsupported type : %s", returnClass);
        }
    }

    /**
     * build set result method interceptor
     */
    private static SqlInvoke buildSet(
            Type type
            , NamingStrategy naming
            , ResultSetConverter converter
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findAllMapSet(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                return (service, sql, arguments, originArguments) -> service.findSet(returnClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(naming, converter, returnClass);
                return (service, sql, arguments, originArguments) -> service.findSet(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SqlInvoke buildList(
            Type type
            , NamingStrategy naming
            , ResultSetConverter converter
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findAllMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                return (service, sql, arguments, originArguments) -> service.findAll(returnClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(naming, converter, returnClass);
                return (service, sql, arguments, originArguments) -> service.findAll(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SqlInvoke buildPage(
            Type type
            , NamingStrategy naming
            , ResultSetConverter converter
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments, originArguments) ->
                        service.pageMap(valueClass, sql, applyPagination(paginationHandler, originArguments), arguments);
            } else {
                return (service, sql, arguments, originArguments) ->
                        service.pageMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments) ->
                            service.page(returnClass, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments) ->
                            service.page(returnClass, sql, applySort(sortHandler, originArguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(naming, converter, returnClass);
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments) ->
                            service.page(sqlStore, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments) ->
                            service.page(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
                }
            }
        }
    }

    /**
     * build next result method interceptor
     */
    private static SqlInvoke buildNext(
            Type type
            , NamingStrategy naming
            , ResultSetConverter converter
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments, originArguments)
                        -> service.nextMap(valueClass, sql, applyPagination(paginationHandler, originArguments), arguments);
            } else {
                return (service, sql, arguments, originArguments)
                        -> service.nextMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments)
                            -> service.next(returnClass, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments)
                            -> service.next(returnClass, sql, applySort(sortHandler, originArguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(naming, converter, returnClass);
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments)
                            -> service.next(sqlStore, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments)
                            -> service.next(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
                }
            }
        }
    }

    private static Pagination applyPagination(ArgumentHandler<Pagination> handler, Object[] arguments) {
        if (handler == null) return null;
        return handler.apply(arguments);
    }

    private static Sort applySort(ArgumentHandler<Sort> handler, Object[] arguments) {
        if (handler == null) return null;
        return handler.apply(arguments);
    }


    private static Function<Object[], String> buildSqlProcessor(
            String command
            , Map<String, ArgumentHandler<?>> handlerMap
    ) {
        char[] cs = command.toCharArray();
        if (SQLQueryReplaceBuilder.hasKey(cs)) {
            SQLQueryReplaceBuilder sqlQueryReplaceBuilder = SQLQueryReplaceBuilder.create(cs);
            Map<String, ArgumentHandler<?>> replaces = new HashMap<>();
            handlerMap.forEach((name, handler) -> {
                if (sqlQueryReplaceBuilder.isKey(name)) {
                    replaces.put(name, handler);
                }
            });

            return (arguments) -> SQLQueryUtil.to(sqlQueryReplaceBuilder, arguments, replaces, handlerMap).toString();
        } else if (SQLQueryBuilder.hasKey(cs)) {
            SQLQueryBuilder sqlQueryBuilder = SQLQueryBuilder.create(cs);
            return (arguments) -> SQLQueryUtil.to(sqlQueryBuilder, arguments, handlerMap).toString();
        }
        return null;
    }

    @NonNull
    private static Class<?> getValueClass(Type type, int index) {
        Type result = getValueType(type, index);
        if (result instanceof ParameterizedType) {
            return ClassUtil.toClassType(((ParameterizedType) result).getRawType());
        } else {
            return ClassUtil.toClassType(result);
        }
    }

    @NonNull
    private static Type getValueType(Type type, int index) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[index];
        }
        return Object.class;
    }

    private static String clearCommand(String command) {
        char[] cs = command.toCharArray();
        char c;
        int count = 0;
        char[] temp = new char[cs.length];
        boolean pause = false;
        boolean space = false;
        for (int i = 0; i < cs.length; i++) {
            c = cs[i];
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    if (!pause) {
                        space = count > 0;
                        break;
                    }
                default:
                    if (space) {
                        temp[count++] = ' ';
                        space = false;
                    }
                    temp[count++] = c;
                    if (c == '\'') {
                        pause = !pause;
                    } else if (c == '\\' && cs[i + 1] == '\'') {
                        temp[count++] = '\'';
                        i++;
                    }
            }
        }
        return new String(temp, 0, count);
    }

}
