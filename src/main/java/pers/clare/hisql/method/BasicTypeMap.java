package pers.clare.hisql.method;

public class BasicTypeMap extends SQLSelectMethod {

    BasicTypeMap(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.find(readonly, sql, returnType, arguments);
    }
}
