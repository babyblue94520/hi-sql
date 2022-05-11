package pers.clare.hisql.method;

public class SQLUpdateMethod extends SQLMethod {
    public SQLUpdateMethod(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.update(sql, arguments);
    }
}
