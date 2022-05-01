package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeMapNext extends PageMethod {

    BasicTypeMapNext(Class<?> returnType) {
        super(returnType);
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.next(readonly, returnType, sql, pagination, arguments);
    }
}
