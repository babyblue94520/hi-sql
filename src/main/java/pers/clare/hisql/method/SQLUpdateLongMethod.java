package pers.clare.hisql.method;

public class SQLUpdateLongMethod extends SQLMethod {
    public SQLUpdateLongMethod() {
        super(Long.class);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return (long) sqlStoreService.update(sql, arguments);
    }
}
