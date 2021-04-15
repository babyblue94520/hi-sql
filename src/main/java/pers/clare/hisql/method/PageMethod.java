package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.page.Pagination;

public abstract class PageMethod extends SQLMethod {

    @Override
    public Object invoke(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        Pagination pagination = getPagination(arguments);
        SQLQuery query = null;
        if (sqlQueryReplaceBuilder != null) {
            query = toSqlQuery(sqlQueryReplaceBuilder, arguments);
        } else if (sqlQueryBuilder != null) {
            query = toSqlQuery(sqlQueryBuilder, arguments);
        }
        if (query == null) {
            return doInvoke(sql, pagination, arguments);
        } else {
            return doInvoke(query.toString(), pagination, emptyArguments);
        }
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return null;
    }

    abstract protected Object doInvoke(String sql, Pagination pagination, Object[] arguments);
}
