package pers.clare.hisql.method;

public class BasicTypeList extends SQLSelectMethod {


    BasicTypeList(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAll(valueType, sql, arguments);
    }
}
