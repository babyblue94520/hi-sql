package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
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
import pers.clare.hisql.util.MethodUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;


public class SQLMethodFactory {

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        buildSqlInvoke(methodInterceptors, sqlStoreService, repositoryInterface);
        return methodInterceptors;
    }

    private static void buildSqlInvoke(
            Map<Method, MethodInterceptor> methodInterceptors
            , SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        if (repositoryInterface == null
                || repositoryInterface == SQLRepository.class
                || repositoryInterface == SQLCrudRepository.class
        ) return;
        Class<?>[] superInterfaces = repositoryInterface.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            buildSqlInvoke(methodInterceptors, sqlStoreService, superInterface);
        }
        Method[] methods = repositoryInterface.getDeclaredMethods();
        Map<String, String> commandMap = SQLInjector.getContents(sqlStoreService.getContext().getXmlRoot(), repositoryInterface);
        for (Method method : methods) {
            if (methodInterceptors.containsKey(method)) continue;
            HiSql hiSql = method.getAnnotation(HiSql.class);
            String command = null;
            if (hiSql != null) {
                command = hiSql.value();
                if (!StringUtils.hasLength(command)) {
                    command = commandMap.get(hiSql.name());
                }
            }
            if (!StringUtils.hasLength(command)) {
                command = commandMap.get(method.getName());
            }
            if (!StringUtils.hasLength(command)) {
                throw new HiSqlException("%s.%s method must set XML or Sql.query", repositoryInterface.getName(), method.getName());
            }
            methodInterceptors.put(method, buildInvoke(sqlStoreService, method, command));
        }
    }

    private static MethodInterceptor buildInvoke(
            SQLStoreService sqlStoreService
            , Method method
            , String command
    ) {
        HiSqlContext context = sqlStoreService.getContext();
        int commandType = SQLQueryUtil.getCommandType(command);
        ArgumentParseUtil.ParseResult parseResult = ArgumentParseUtil.build(method);
        List<BiConsumer<Object[], StringBuilder>> sqlProcessors = buildSqlProcessor(
                command
                , parseResult.getGetters()
        );

        SqlInvoke sqlInvoke = parseResult.hasCallback() ?
                buildCallbackSqlInvoke(
                        commandType
                        , parseResult.getConnection()
                        , parseResult.getPreparedStatement()
                        , parseResult.getResultSet()
                )
                :
                buildSqlInvoke(
                        method
                        , commandType
                        , context
                        , parseResult.getPagination()
                        , parseResult.getSort()
                );


        if (sqlInvoke == null) {
            throw new HiSqlException("%s not support return type", method.getName());
        }
        return (invocation) -> {
            Object[] arguments = invocation.getArguments();
            String sql = getRealSql(command, sqlProcessors, arguments).toString();
            return sqlInvoke.apply(sqlStoreService, sql, arguments);
        };
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
    private static SqlInvoke buildSqlInvoke(
            Method method
            , int commandType
            , HiSqlContext context
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnType = ClassUtil.toClassType(method.getReturnType());
        switch (commandType) {
            case CommandType.Select:
                if (Collection.class.isAssignableFrom(returnType)) {
                    if (returnType == Set.class) {
                        return buildSet(method, context, sortHandler);
                    } else {
                        return buildList(method, context, sortHandler);
                    }
                } else if (returnType.isArray()) {
                    return buildList(method, context, sortHandler);
                } else if (returnType == Map.class) {
                    Class<?> type = getMapValueClass(method);
                    return (service, sql, arguments) -> service.find(sql, applySort(sortHandler, arguments), type, arguments);
                } else if (Page.class.isAssignableFrom(returnType)) {
                    return buildPage(method, context, paginationHandler, sortHandler);
                } else if (Next.class.isAssignableFrom(returnType)) {
                    return buildNext(method, context, paginationHandler, sortHandler);
                } else {
                    if (SQLStoreFactory.isIgnore(returnType)) {
                        return (service, sql, arguments) -> service.findFirst(returnType, sql, applySort(sortHandler, arguments), arguments);
                    } else {
                        SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnType, false);
                        return (service, sql, arguments) -> service.find(sqlStore, sql, applySort(sortHandler, arguments), arguments);
                    }
                }
            case CommandType.Insert:
                return (service, sql, arguments) -> service.insert(returnType, sql, arguments);
            default:
                return SQLService::update;
        }
    }

    /**
     * build set result method interceptor
     */
    private static SqlInvoke buildSet(
            Method method
            , HiSqlContext context
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            Class<?> type = getParameterMapValueClass(method, returnType);
            return (service, sql, arguments) -> service.findAllMapSet(type, sql, applySort(sortHandler, arguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return (service, sql, arguments) -> service.findSet(returnType, sql, applySort(sortHandler, arguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnType, false);
                return (service, sql, arguments) -> service.findSet(sqlStore, sql, applySort(sortHandler, arguments), arguments);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SqlInvoke buildList(
            Method method
            , HiSqlContext context
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            Class<?> type = getParameterMapValueClass(method, returnType);
            return (service, sql, arguments) -> service.findAllMap(type, sql, applySort(sortHandler, arguments), arguments);
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return (service, sql, arguments) -> service.findAll(returnType, sql, applySort(sortHandler, arguments), arguments);
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnType, false);
                return (service, sql, arguments) -> service.findAll(sqlStore, sql, applySort(sortHandler, arguments), arguments);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SqlInvoke buildPage(
            Method method
            , HiSqlContext context
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            Class<?> type = getParameterMapValueClass(method, null);
            if (paginationHandler != null) {
                return (service, sql, arguments) ->
                        service.pageMap(type, sql, applyPagination(paginationHandler, arguments), arguments);
            } else {
                return (service, sql, arguments) ->
                        service.pageMap(type, sql, applySort(sortHandler, arguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments) ->
                            service.page(returnType, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments) ->
                            service.page(returnType, sql, applySort(sortHandler, arguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnType, false);
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
            Method method
            , HiSqlContext context
            , ArgumentHandler<Pagination> paginationHandler
            , ArgumentHandler<Sort> sortHandler
    ) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            Class<?> type = getParameterMapValueClass(method, null);
            if (paginationHandler != null) {
                return (service, sql, arguments)
                        -> service.nextMap(type, sql, applyPagination(paginationHandler, arguments), arguments);
            } else {
                return (service, sql, arguments)
                        -> service.nextMap(type, sql, applySort(sortHandler, arguments), arguments);
            }
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                if (paginationHandler != null) {
                    return (service, sql, arguments)
                            -> service.next(returnType, sql, applyPagination(paginationHandler, arguments), arguments);
                } else {
                    return (service, sql, arguments)
                            -> service.next(returnType, sql, applySort(sortHandler, arguments), arguments);
                }
            } else {
                SQLStore<?> sqlStore = SQLStoreFactory.build(context, returnType, false);
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
        return  handler.apply(arguments);
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
        if (SQLQueryReplaceBuilder.findKeyCount(cs) > 0) {
            SQLQueryReplaceBuilder sqlQueryReplaceBuilder = new SQLQueryReplaceBuilder(cs);
            Map<String, ArgumentHandler<?>> replaces = new HashMap<>();
            handlerMap.forEach((name, handler) -> {
                if (sqlQueryReplaceBuilder.hasKey(name)) {
                    replaces.put(name, handler);
                }
            });

            sqlProcessors.add((arguments, sql) -> {
                sql.setLength(0);
                sql.append(SQLQueryUtil.toSqlQuery(sqlQueryReplaceBuilder, arguments, replaces, handlerMap).toSQL());
            });

        } else if (SQLQueryBuilder.findKeyCount(cs) > 0) {
            SQLQueryBuilder sqlQueryBuilder = new SQLQueryBuilder(cs);
            sqlProcessors.add((arguments, sql) -> {
                sql.setLength(0);
                sql.append(SQLQueryUtil.toSqlQuery(sqlQueryBuilder, arguments, handlerMap).toSQL());
            });
        }
        return sqlProcessors;
    }

    private static Class<?> getMapValueClass(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 1);
        return returnType == null ? Object.class : returnType;
    }

    private static Class<?> getParameterMapValueClass(Method method, Class<?> returnType) {
        if (returnType == null) return Object.class;
        returnType = (Class<?>) MethodUtil.getType(MethodUtil.getReturnType(method, 0), 1);
        return returnType == null ? Object.class : returnType;
    }

    public static void main(String[] args) {
        System.out.println(int.class.isPrimitive());
        System.out.println(Integer.class.isPrimitive());
    }
}
