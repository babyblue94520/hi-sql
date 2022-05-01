package pers.clare.hisql.method;

public class SQLEntitySet extends SQLStoreMethod {

    SQLEntitySet(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findSet(readonly, sqlStore, sql, arguments);
    }
}
