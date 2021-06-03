package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypeMapPage extends PageMethod {

    protected Class<?> valueType;

    BasicTypeMapPage(Class<?> valueType) {
        this.valueType = valueType;
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.page(readonly, valueType, sql, pagination, arguments);
    }
}
