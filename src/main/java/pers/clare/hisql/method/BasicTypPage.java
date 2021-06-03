package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;

public class BasicTypPage extends PageMethod {

    protected Class<?> valueType;

    BasicTypPage(Class<?> valueType) {
        this.valueType = valueType;
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.basicPage(readonly, valueType, sql, pagination, arguments);
    }
}
