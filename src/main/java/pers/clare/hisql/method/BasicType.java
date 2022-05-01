package pers.clare.hisql.method;

public class BasicType extends SQLSelectMethod {

    BasicType(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findFirst(readonly, returnType, sql, arguments);
    }
}
