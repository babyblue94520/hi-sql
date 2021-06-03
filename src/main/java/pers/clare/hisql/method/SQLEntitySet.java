package pers.clare.hisql.method;

public class SQLEntitySet extends SQLStoreMethod {

    SQLEntitySet(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findSet(readonly, sqlStore, sql, arguments);
    }
}
