package pers.clare.hisql.method;

public class BasicTypeMapList extends SQLSelectMethod {

    BasicTypeMapList(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAllMap(readonly, returnType, sql, arguments);
    }
}
