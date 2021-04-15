package pers.clare.hisql.method;

public class SQLEntity extends SQLStoreMethod {

    SQLEntity(Class<?> valueType) {
        super(valueType);
    }

    @Override
    protected Object doInvoke(String sql, Object[] arguments) {
        return sqlStoreService.find(sqlStore, sql, arguments);
    }
}
