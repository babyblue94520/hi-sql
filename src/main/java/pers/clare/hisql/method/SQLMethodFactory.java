package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.service.SQLStoreBasicService;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;


public class SQLMethodFactory {
    private static final Logger log = LogManager.getLogger();

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            Class<?> repositoryInterface
            , SQLStoreService service
    ) {
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        buildSqlInvoke(repositoryInterface, service, methodInterceptors);
        return methodInterceptors;
    }

    private static void buildSqlInvoke(
            Class<?> clazz
            , SQLStoreService service
            , Map<Method, MethodInterceptor> methodInterceptors
    ) {
        if (clazz == null
            || clazz == SQLRepository.class
            || clazz == SQLCrudRepository.class
        ) return;
        Class<?>[] superInterfaces = clazz.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            buildSqlInvoke(superInterface, service, methodInterceptors);
        }
        Method[] methods = ClassUtil.getDeclaredMethods(clazz);
        Map<String, String> commandMap = SQLInjector.getContents(service.getXmlRoot(), clazz);
        for (Method method : methods) {
            if (methodInterceptors.containsKey(method)) continue;
            int modifier = method.getModifiers();
            if (Modifier.isStatic(modifier) || !Modifier.isPublic(modifier)) continue;
            boolean optional = false;
            Type returnType = method.getGenericReturnType();
            if (method.getReturnType() == Optional.class) {
                optional = true;
                returnType = ClassUtil.getValueType(returnType, 0);
            }

            ArgumentParseUtil.ParseResult parseResult = ArgumentParseUtil.build(method);
            String command = null;
            boolean autoKey = false;
            HiSql hiSql = method.getAnnotation(HiSql.class);
            if (hiSql != null) {
                command = hiSql.value();
                autoKey = hiSql.returnIncrementKey();
            }
            if (!StringUtils.hasLength(command)) {
                command = commandMap.get(method.getName());
                if (!StringUtils.hasLength(command)) {
                    throw ExceptionUtil.insertAfter(method, new HiSqlException(String.format("%s.%s method must set XML or @HiSql", clazz.getName(), method.getName())));
                }
            }
            command = CommandUtil.clearCommand(command);

            // check start with 'from'
            if (command.charAt(0) == 'f' || command.charAt(0) == 'F') {
                command = CommandUtil.appendSelectColumns(service, returnType, command);
                log.debug("{}.{} append select columns '{}'.", clazz.getSimpleName(), method.getName(), command);
            }

            int commandType = service.getCommandTypeParser().parse(command);

            Function<Object[], String> sqlProcessor = buildSqlProcessor(
                    command
                    , parseResult.getGetters()
            );
            SqlInvoke sqlInvoke = buildInvoke(service, parseResult, returnType, commandType, autoKey);
            if (sqlInvoke == null) {
                throw ExceptionUtil.insertAfter(method, new HiSqlException(String.format("%s.%s not support return type.", clazz.getName(), method.getName())));
            }

            MethodInterceptor interceptor;
            if (sqlProcessor == null) {
                String finalCommand = command;
                if (optional) {
                    interceptor = (invocation) -> Optional.ofNullable(sqlInvoke.apply(service, finalCommand, invocation.getArguments(), invocation.getArguments()));
                } else {
                    interceptor = (invocation) -> sqlInvoke.apply(service, finalCommand, invocation.getArguments(), invocation.getArguments());
                }
            } else {
                if (optional) {
                    interceptor = (invocation) -> Optional.ofNullable(sqlInvoke.apply(service, sqlProcessor.apply(invocation.getArguments()), null, invocation.getArguments()));
                } else {
                    interceptor = (invocation) -> sqlInvoke.apply(service, sqlProcessor.apply(invocation.getArguments()), null, invocation.getArguments());
                }
            }
            methodInterceptors.put(method, interceptor);
        }
    }

    private static SqlInvoke buildInvoke(
            SQLStoreBasicService service
            , ArgumentParseUtil.ParseResult parseResult
            , Type returnType
            , int commandType
            , boolean autoKey
    ) {
        SqlInvoke sqlInvoke = null;
        if (parseResult.hasCallback()) {
            sqlInvoke = buildCallbackSqlInvoke(
                    commandType
                    , parseResult.getConnection()
                    , parseResult.getPreparedStatement()
                    , parseResult.getResultSet()
            );
        } else {
            switch (commandType) {
                case CommandType.Query:
                    sqlInvoke = buildSqlSelectInvoke(
                            returnType
                            , service
                            , parseResult.getPagination()
                            , parseResult.getSort()
                    );
                    break;
                case CommandType.Update:
                    if (autoKey) {
                        sqlInvoke = buildSqlInsertInvoke(returnType);
                    } else {
                        sqlInvoke = buildSqlUpdateInvoke(returnType);
                    }
                    break;
                default:

            }
        }

        return sqlInvoke;

    }

    private static SqlInvoke buildCallbackSqlInvoke(
            int commandType
            , ArgumentHandler<ConnectionCallback<?>> connectionCallback
            , ArgumentHandler<PreparedStatementCallback<?>> preparedStatementCallback
            , ArgumentHandler<ResultSetCallback<?>> resultSetCallback
    ) {
        if (connectionCallback != null) {
            return (service, sql, arguments, originArguments) -> service.connection(sql, originArguments, connectionCallback.apply(originArguments));
        }

        if (preparedStatementCallback != null) {
            return (service, sql, arguments, originArguments) -> service.prepared(sql, preparedStatementCallback.apply(originArguments));
        }

        if (resultSetCallback != null) {
            if (commandType == CommandType.Query) {
                return (service, sql, arguments, originArguments) -> service.query(sql, originArguments, resultSetCallback.apply(originArguments));
            }
        }
        return null;
    }


    /**
     * build method interceptor by command type
     */
    private static SqlInvoke buildSqlSelectInvoke(
            Type type
            , SQLStoreBasicService basicService
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.toWrapperClass(type);
        if (Collection.class.isAssignableFrom(returnClass)) {
            if (returnClass == Set.class) {
                return buildSet(type, basicService, sortHandler);
            } else {
                return buildList(type, basicService, sortHandler);
            }
        } else if (returnClass.isArray()) {
            return buildList(type, basicService, sortHandler);
        } else if (returnClass == Map.class) {
            Class<?> valueClass = ClassUtil.getValueClass(ClassUtil.getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else if (Page.class.isAssignableFrom(returnClass)) {
            return buildPage(type, basicService, paginationHandler, sortHandler);
        } else if (Next.class.isAssignableFrom(returnClass)) {
            return buildNext(type, basicService, paginationHandler, sortHandler);
        } else {
            if (returnClass.isPrimitive()) {
                final Class<?> objectClass = ClassUtil.toWrapperClass(returnClass);
                return (service, sql, arguments, originArguments) -> {
                    Object result = service.find(objectClass, sql, applySort(sortHandler, originArguments), arguments);
                    return Objects.requireNonNullElse(result, ClassUtil.getDefaultValue(returnClass, result));
                };
            } else if (SQLStoreUtil.isIgnore(returnClass)) {
                final Class<?> objectClass = ClassUtil.toWrapperClass(returnClass);
                return (service, sql, arguments, originArguments) -> service.find(objectClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = basicService.build(returnClass);
                return (service, sql, arguments, originArguments) -> service.find(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    private static SqlInvoke buildSqlInsertInvoke(
            Type type
    ) {
        Class<?> keyClass = ClassUtil.toWrapperClass(type);
        return (service, sql, arguments, originArguments) -> service.insert(keyClass, sql, arguments);
    }

    /**
     * build method interceptor by command type
     */
    private static SqlInvoke buildSqlUpdateInvoke(
            Type type
    ) {
        Class<?> returnClass = ClassUtil.toWrapperClass(type);
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
            , SQLStoreBasicService basicService
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = ClassUtil.getValueClass(ClassUtil.getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findAllMapSet(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else {
            if (SQLStoreUtil.isIgnore(returnClass)) {
                return (service, sql, arguments, originArguments) -> service.findSet(returnClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = basicService.build(returnClass);
                return (service, sql, arguments, originArguments) -> service.findSet(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SqlInvoke buildList(
            Type type
            , SQLStoreBasicService basicService
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = ClassUtil.getValueClass(ClassUtil.getValueType(type, 0), 1);
            return (service, sql, arguments, originArguments) -> service.findAllMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
        } else {
            if (SQLStoreUtil.isIgnore(returnClass)) {
                return (service, sql, arguments, originArguments) -> service.findAll(returnClass, sql, applySort(sortHandler, originArguments), arguments);
            } else {
                SQLStore<?> sqlStore = basicService.build(returnClass);
                return (service, sql, arguments, originArguments) -> service.findAll(sqlStore, sql, applySort(sortHandler, originArguments), arguments);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SqlInvoke buildPage(
            Type type
            , SQLStoreBasicService basicService
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = ClassUtil.getValueClass(ClassUtil.getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments, originArguments) ->
                        service.pageMap(valueClass, sql, applyPagination(paginationHandler, originArguments), arguments);
            } else {
                return (service, sql, arguments, originArguments) ->
                        service.pageMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
            }
        } else {
            if (SQLStoreUtil.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments) ->
                            service.page(returnClass, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments) ->
                            service.page(returnClass, sql, applySort(sortHandler, originArguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = basicService.build(returnClass);
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
            , SQLStoreBasicService basicService
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnClass = ClassUtil.getValueClass(type, 0);
        if (returnClass == Map.class) {
            Class<?> valueClass = ClassUtil.getValueClass(ClassUtil.getValueType(type, 0), 1);
            if (paginationHandler != null) {
                return (service, sql, arguments, originArguments)
                        -> service.nextMap(valueClass, sql, applyPagination(paginationHandler, originArguments), arguments);
            } else {
                return (service, sql, arguments, originArguments)
                        -> service.nextMap(valueClass, sql, applySort(sortHandler, originArguments), arguments);
            }
        } else {
            if (SQLStoreUtil.isIgnore(returnClass)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments, originArguments)
                            -> service.next(returnClass, sql, applyPagination(paginationHandler, originArguments), arguments);
                } else {
                    return (service, sql, arguments, originArguments)
                            -> service.next(returnClass, sql, applySort(sortHandler, originArguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = basicService.build(returnClass);
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
            return (arguments) -> SQLQueryUtil.to(sqlQueryReplaceBuilder, arguments, handlerMap).toString();
        } else if (SQLQueryBuilder.hasKey(cs)) {
            SQLQueryBuilder sqlQueryBuilder = SQLQueryBuilder.create(cs);
            return (arguments) -> SQLQueryUtil.to(sqlQueryBuilder, arguments, handlerMap).toString();
        }
        return null;
    }

}
