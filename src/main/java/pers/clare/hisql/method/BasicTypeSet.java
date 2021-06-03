package pers.clare.hisql.method;


public class BasicTypeSet extends SQLSelectMethod {

    BasicTypeSet(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findSet(readonly, valueType, sql, arguments);
    }
}
