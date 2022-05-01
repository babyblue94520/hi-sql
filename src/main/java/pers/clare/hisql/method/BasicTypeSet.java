package pers.clare.hisql.method;


public class BasicTypeSet extends SQLSelectMethod {

    BasicTypeSet(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findSet(readonly, returnType, sql, arguments);
    }
}
