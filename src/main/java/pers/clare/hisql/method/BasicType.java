package pers.clare.hisql.method;

public class BasicType extends SQLSelectMethod {

    BasicType(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findFirst(readonly, valueType, sql, arguments);
    }
}
