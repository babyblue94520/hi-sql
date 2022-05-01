package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypPage extends PageMethod {

    BasicTypPage(Class<?> returnType) {
        super(returnType);
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.basicPage(readonly, returnType, sql, pagination, arguments);
    }
}
