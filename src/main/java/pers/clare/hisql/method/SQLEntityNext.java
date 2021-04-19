package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;


public class SQLEntityNext extends PageMethod {
    protected Class<?> valueType;
    protected SQLStore<?> sqlStore;

    SQLEntityNext(Class<?> valueType) {
        this.valueType = valueType;
    }

    @Override
    public void init() {
        this.sqlStore = SQLStoreFactory.build(context, valueType, false);
        super.init();
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.next(this.sqlStore, sql, pagination, arguments);
    }
}
