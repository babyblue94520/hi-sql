package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeMapNext extends PageMethod {

    protected Class<?> valueType;

    BasicTypeMapNext(Class<?> valueType) {
        this.valueType = valueType;
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.next(this.valueType, sql, pagination, arguments);
    }
}
