package pers.clare.hisql.method;

public class BasicTypeMapSet extends SQLSelectMethod {

    BasicTypeMapSet(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAllMapSet(valueType, sql, arguments);
    }
}
