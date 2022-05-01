package pers.clare.hisql.method;

public class SQLEntityList extends SQLStoreMethod {

    SQLEntityList(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.findAll(readonly, sqlStore, sql, arguments);
    }
}
