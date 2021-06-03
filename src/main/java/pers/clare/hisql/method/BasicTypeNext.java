package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeNext extends PageMethod {

    protected Class<?> valueType;

    BasicTypeNext(Class<?> valueType) {
        this.valueType = valueType;
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.basicNext(readonly, valueType, sql, pagination, arguments);
    }
}
