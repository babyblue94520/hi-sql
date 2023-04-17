package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.constant.CommandType;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.*;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.ArgumentParseUtil;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.ExceptionUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;


public class SQLMethodFactory {

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
        Method[] methods = clazz.getDeclaredMethods();
        Map<String, String> commandMap = SQLInjector.getContents(sqlStoreService.getContext().getXmlRoot(), clazz);
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
        int commandType = SQLQueryUtil.getCommandType(command);
        ArgumentParseUtil.ParseResult parseResult = ArgumentParseUtil.build(method);
        List<BiConsumer<Object[], StringBuilder>> sqlProcessors = buildSqlProcessor(
                command
                , parseResult.getGetters()
        );

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
                case CommandType.Select:
                    sqlInvoke = buildSqlSelectInvoke(
                            type
                            , sqlStoreService
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
            throw new HiSqlException("%s not support return type", method.getName());
        }
        if (optional) {
            return (invocation) -> {
                Object[] arguments = invocation.getArguments();
                String sql = getRealSql(command, sqlProcessors, arguments).toString();
                return Optional.ofNullable(sqlInvoke.apply(sqlStoreService, sql, arguments));
            };
        } else {
            return (invocation) -> {
                Object[] arguments = invocation.getArguments();
                String sql = getRealSql(command, sqlProcessors, arguments).toString();
                return sqlInvoke.apply(sqlStoreService, sql, arguments);
            };
        }
    }

    private static SqlInvoke buildCallbackSqlInvoke(
            int commandType
            , ArgumentHandler<ConnectionCallback<?>> connectionCallback
            , ArgumentHandler<PreparedStatementCallback<?>> preparedStatementCallback
            , ArgumentHandler<ResultSetCallback<?>> resultSetCallback
    ) {
        if (connectionCallback != null) {
            return (service, sql, arguments) -> service.connection(sql, arguments, connectionCallback.apply(arguments));
        }

        if (preparedStatementCallback != null) {
            return (service, sql, arguments) -> service.prepared(sql, preparedStatementCallback.apply(arguments));
        }

        if (resultSetCallback != null) {
            switch (commandType) {
                case CommandType.Select:
                    return (service, sql, arguments) -> service.query(sql, arguments, resultSetCallback.apply(arguments));
                case CommandType.Insert:
                    return SQLService::insert;
                default:
                    return SQLService::update;
            }
        }
        return null;
    }


    /**
     * build method interceptor by command type
     */
    private static SqlInvoke buildSqlSelectInvoke(
            Type type
            , SQLStoreService sqlStoreService
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.toClassType(type);
        if (Collection.class.isAssignableFrom(returnClass)) {
            if (returnClass == Set.class) {
                return buildSet(type, sqlStoreService, sortHandler);
            } else {
                return buildList(type, sqlStoreService, sortHandler);
            }
        } else if (returnClass.isArray()) {
            return buildList(type, sqlStoreService, sortHandler);
        } else if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments) -> service.findMap(valueClass, sql, applySort(sortHandler, arguments), arguments);
        } else if (Page.class.isAssignableFrom(returnClass)) {
            return buildPage(type, context, paginationHandler, sortHandler);
        } else if (Next.class.isAssignableFrom(returnClass)) {
            return buildNext(type, context, paginationHandler, sortHandler);
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                return (service, sql, arguments) -> service.find(returnClass, sql, applySort(sortHandler, arguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(sqlStoreService, returnClass, false);
                return (service, sql, arguments) -> service.find(sqlStore, sql, applySort(sortHandler, arguments), arguments);
            }
        }
    }

    private static SqlInvoke buildSqlInsertInvoke(
            Type type
            , boolean autoKey
    ) {
        Class<?> returnClass = ClassUtil.toClassType(type);
        if (autoKey) {
            return (service, sql, arguments) -> service.insert(returnClass, sql, arguments);
        } else {
            if (returnClass == int.class || returnClass == Integer.class) {
                return SQLService::insert;
            } else if (returnClass == long.class || returnClass == Long.class) {
                return SQLService::insertLarge;
            } else if (returnClass == void.class) {
                return SQLService::insert;
            } else {
                throw new HiSqlException("Unsupported type : %s", returnClass);
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
        if (returnClass == int.class || returnClass == Integer.class) {
            return SQLService::update;
        } else if (returnClass == long.class || returnClass == Long.class) {
            return SQLService::updateLarge;
        } else if (returnClass == void.class) {
            return SQLService::update;
        } else {
            throw new HiSqlException("Unsupported type : %s", returnClass);
        }
    }

    /**
     * build set result method interceptor
     */
    private static SqlInvoke buildSet(
            Type type
            , HiSqlContext context
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments) -> service.findAllMapSet(valueClass, sql, applySort(sortHandler, arguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                return (service, sql, arguments) -> service.findSet(returnClass, sql, applySort(sortHandler, arguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnClass, false);
                return (service, sql, arguments) -> service.findSet(sqlStore, sql, applySort(sortHandler, arguments), arguments);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SqlInvoke buildList(
            Type type
            , HiSqlContext context
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            return (service, sql, arguments) -> service.findAllMap(valueClass, sql, applySort(sortHandler, arguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                return (service, sql, arguments) -> service.findAll(returnClass, sql, applySort(sortHandler, arguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnClass, false);
                return (service, sql, arguments) -> service.findAll(sqlStore, sql, applySort(sortHandler, arguments), arguments);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SqlInvoke buildPage(
            Type type
            , HiSqlContext context
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments) ->
                        service.pageMap(valueClass, sql, applyPagination(paginationHandler, arguments), arguments);
            } else {
                return (service, sql, arguments) ->
                        service.pageMap(valueClass, sql, applySort(sortHandler, arguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments) ->
                            service.page(returnClass, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments) ->
                            service.page(returnClass, sql, applySort(sortHandler, arguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnClass, false);
                if (paginationHandler != null) {
                    return (service, sql, arguments) ->
                            service.page(sqlStore, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments) ->
                            service.page(sqlStore, sql, applySort(sortHandler, arguments), arguments);
                }
            }
        }
    }

    /**
     * build next result method interceptor
     */
    private static SqlInvoke buildNext(
            Type type
            , HiSqlContext context
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = getValueClass(getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments)
                        -> service.nextMap(valueClass, sql, applyPagination(paginationHandler, arguments), arguments);
            } else {
                return (service, sql, arguments)
                        -> service.nextMap(valueClass, sql, applySort(sortHandler, arguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments)
                            -> service.next(returnClass, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments)
                            -> service.next(returnClass, sql, applySort(sortHandler, arguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnClass, false);
                if (paginationHandler != null) {
                    return (service, sql, arguments)
                            -> service.next(sqlStore, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments)
                            -> service.next(sqlStore, sql, applySort(sortHandler, arguments), arguments);
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

    private static StringBuilder getRealSql(
            String sql
            , List<BiConsumer<Object[], StringBuilder>> sqlProcessors
            , Object[] arguments
    ) {
        StringBuilder realSql = new StringBuilder(sql);
        for (BiConsumer<Object[], StringBuilder> sqlProcessor : sqlProcessors) {
            sqlProcessor.accept(arguments, realSql);
        }
        return realSql;
    }

    private static List<BiConsumer<Object[], StringBuilder>> buildSqlProcessor(
            String command
            , Map<String, ArgumentHandler<?>> handlerMap
    ) {
        List<BiConsumer<Object[], StringBuilder>> sqlProcessors = new ArrayList<>();
        char[] cs = command.toCharArray();
        if (SQLQueryReplaceBuilder.hasKey(cs)) {
            SQLQueryReplaceBuilder sqlQueryReplaceBuilder = SQLQueryReplaceBuilder.create(cs);
            Map<String, ArgumentHandler<?>> replaces = new HashMap<>();
            handlerMap.forEach((name, handler) -> {
                if (sqlQueryReplaceBuilder.isKey(name)) {
                    replaces.put(name, handler);
                }
            });

            sqlProcessors.add((arguments, sql) -> {
                sql.setLength(0);
                sql.append(SQLQueryUtil.to(sqlQueryReplaceBuilder, arguments, replaces, handlerMap).toSQL());
            });
        } else if (SQLQueryBuilder.hasKey(cs)) {
            SQLQueryBuilder sqlQueryBuilder = SQLQueryBuilder.create(cs);
            sqlProcessors.add((arguments, sql) -> {
                sql.setLength(0);
                sql.append(SQLQueryUtil.to(sqlQueryBuilder, arguments, handlerMap).toSQL());
            });
        }
        return sqlProcessors;
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

}
