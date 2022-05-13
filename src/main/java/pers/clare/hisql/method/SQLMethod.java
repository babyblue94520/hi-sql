package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.constant.CommandType;
import pers.clare.hisql.function.ArgumentGetHandler;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.util.ArgumentGetHandlerUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLMethod implements MethodInterceptor {
    protected static final Object[] emptyArguments = new Object[0];
    protected final Class<?> returnType;
    protected Method method;
    protected int commandType;
    protected SQLStoreService sqlStoreService;
    protected String sql;
    protected boolean readonly;
    protected SQLQueryReplaceBuilder sqlQueryReplaceBuilder;
    protected SQLQueryBuilder sqlQueryBuilder;
    protected Map<String, ArgumentGetHandler> replaces;
    protected Map<String, ArgumentGetHandler> valueHandlers;
    protected ArgumentGetHandler paginationHandler;
    protected ArgumentGetHandler sortHandler;
    protected ArgumentGetHandler connectionCallback;
    protected ArgumentGetHandler preparedStatementCallback;
    protected ArgumentGetHandler resultSetCallback;

    protected SQLMethod(Class<?> returnType) {
        this.returnType = returnType;
    }

    void init() {
        ArgumentGetHandlerUtil.ArgumentGetterResult result = ArgumentGetHandlerUtil.build(method);
        valueHandlers = result.getGetters();
        paginationHandler = result.getPagination();
        sortHandler = result.getSort();
        connectionCallback = result.getConnection();
        preparedStatementCallback = result.getPreparedStatement();
        resultSetCallback = result.getResultSet();

        char[] cs = sql.toCharArray();
        if (SQLQueryReplaceBuilder.findKeyCount(cs) > 0) {
            sqlQueryReplaceBuilder = new SQLQueryReplaceBuilder(cs);
            this.replaces = new HashMap<>();
            valueHandlers.forEach((name, handler) -> {
                if (sqlQueryReplaceBuilder.hasKey(name)) {
                    replaces.put(name, handler);
                }
            });
        } else if (SQLQueryBuilder.findKeyCount(cs) > 0) {
            sqlQueryBuilder = new SQLQueryBuilder(cs);
        }
    }

    void setSqlStoreService(SQLStoreService sqlStoreService) {
        this.sqlStoreService = sqlStoreService;
    }

    void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    void setSql(String sql) {
        this.sql = sql;
    }

    void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    void setMethod(Method method) {
        this.method = method;
    }

    Pagination getPagination(Object[] arguments) {
        if (paginationHandler == null) return null;
        return (Pagination) paginationHandler.apply(arguments);
    }

    Sort getSort(Object[] arguments) {
        if (sortHandler == null) return null;
        return (Sort) sortHandler.apply(arguments);
    }


    @Override
    public Object invoke(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        Pagination pagination = getPagination(arguments);
        PaginationMode paginationMode = sqlStoreService.getContext().getPaginationMode();
        Sort sort = getSort(arguments);
        SQLQuery query = toSqlQuery(arguments);
        String executeSQL = this.sql;
        if (query == null) {
            if (pagination != null) {
                executeSQL = paginationMode.buildPaginationSQL(pagination, executeSQL);
            } else if (sort != null) {
                executeSQL = paginationMode.buildSortSQL(sort, executeSQL);
            }
        } else {
            StringBuilder sb = query.toSQL();
            if (pagination != null) {
                paginationMode.appendPaginationSQL(sb, pagination);
            } else if (sort != null) {
                paginationMode.appendSortSQL(sb, sort.getSorts());
            }
            executeSQL = sb.toString();
            arguments = emptyArguments;
        }
        if (connectionCallback != null) {
            return sqlStoreService.connection(readonly, executeSQL, methodInvocation.getArguments()
                    , (ConnectionCallback<?>) connectionCallback.apply(methodInvocation.getArguments())
            );
        }
        if (preparedStatementCallback != null) {
            return sqlStoreService.prepared(readonly, executeSQL
                    , (PreparedStatementCallback<?>) preparedStatementCallback.apply(methodInvocation.getArguments())
            );
        }
        if (resultSetCallback != null) {
            switch (commandType) {
                case CommandType.Select:
                    return sqlStoreService.query(readonly, executeSQL, arguments
                            , (ResultSetCallback<?>) resultSetCallback.apply(methodInvocation.getArguments()));
                case CommandType.Insert:
                    return sqlStoreService.insert(executeSQL, arguments
                            , (ResultSetCallback<?>) resultSetCallback.apply(methodInvocation.getArguments()));
                default:
                    return sqlStoreService.update(executeSQL, arguments
                            , (ResultSetCallback<?>) resultSetCallback.apply(methodInvocation.getArguments()));
            }
        }
        return doInvoke(executeSQL, arguments);
    }

    protected SQLQuery toSqlQuery(Object[] arguments) {
        if (sqlQueryReplaceBuilder != null) {
            return SQLQueryUtil.toSqlQuery(sqlQueryReplaceBuilder, arguments, replaces, valueHandlers);
        } else if (sqlQueryBuilder != null) {
            return SQLQueryUtil.toSqlQuery(sqlQueryBuilder, arguments, valueHandlers);
        } else {
            return null;
        }
    }

    abstract protected Object doInvoke(String sql, Object[] arguments);
}
