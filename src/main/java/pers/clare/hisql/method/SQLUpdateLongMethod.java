package pers.clare.hisql.method;

public class SQLUpdateLongMethod extends SQLMethod {
    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return Long.valueOf(sqlStoreService.update(sql, arguments));
    }
}
