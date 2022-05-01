package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeMapPage extends PageMethod {

    BasicTypeMapPage(Class<?> returnType) {
        super(returnType);
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.page(readonly, returnType, sql, pagination, arguments);
    }
}
