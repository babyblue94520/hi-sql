package pers.clare.hisql.method;

import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.page.Pagination;


public class SQLEntityPage extends PageMethod {
    protected Class<?> valueType;
    protected SQLStore<?> sqlStore;

    SQLEntityPage(Class<?> valueType) {
        this.valueType = valueType;
    }

    @Override
    public void init() {
        this.sqlStore = SQLStoreFactory.build(context, valueType, false);
        super.init();
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.page(this.sqlStore, sql, pagination, arguments);
    }
}
