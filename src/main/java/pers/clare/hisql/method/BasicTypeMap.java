package pers.clare.hisql.method;

public class BasicTypeMap extends SQLSelectMethod {


    BasicTypeMap(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.find(sql, valueType, arguments);
    }
}
