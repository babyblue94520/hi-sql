package pers.clare.hisql.method;

public class BasicTypeMapSet extends SQLSelectMethod {

    BasicTypeMapSet(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAllMapSet(readonly, returnType, sql, arguments);
    }
}
