package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.query.SQLQuery;

public abstract class PageMethod extends SQLMethod {

    public PageMethod(Class<?> returnType) {
        super(returnType);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        SQLQuery query = toSqlQuery(arguments);
        if (query == null) {
            return doInvoke(sql, getPagination(arguments), arguments);
        } else {
            return doInvoke(query.toString(), getPagination(arguments), emptyArguments);
        }
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return null;
    }

    abstract protected Object doInvoke(String sql, Pagination pagination, Object[] arguments);
}
