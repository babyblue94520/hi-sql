package pers.clare.hisql.method;

public class SQLInsertMethod extends SQLMethod {
    public SQLInsertMethod(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.insert(sql, returnType, arguments);
    }
}
