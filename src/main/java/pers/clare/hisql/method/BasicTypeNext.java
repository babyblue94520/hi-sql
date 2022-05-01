package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeNext extends PageMethod {

    BasicTypeNext(Class<?> returnType) {
        super(returnType);
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.basicNext(readonly, returnType, sql, pagination, arguments);
    }
}
