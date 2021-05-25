package pers.clare.hisql.method;

public class SQLUpdateLongMethod extends SQLMethod {
    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return (long) sqlStoreService.update(sql, arguments);
    }
}
