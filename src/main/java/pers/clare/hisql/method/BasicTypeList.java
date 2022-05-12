package pers.clare.hisql.method;

public class BasicTypeList extends SQLSelectMethod {

    BasicTypeList(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAll(readonly, returnType, sql, arguments);
    }
}
