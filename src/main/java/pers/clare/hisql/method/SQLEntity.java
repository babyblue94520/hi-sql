package pers.clare.hisql.method;

public class SQLEntity extends SQLStoreMethod {

    SQLEntity(Class<?> returnType) {
        super(returnType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.find(sqlStore, sql, arguments);
    }
}
