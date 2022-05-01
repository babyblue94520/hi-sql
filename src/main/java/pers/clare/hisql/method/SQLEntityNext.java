package pers.clare.hisql.method;

import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;


public class SQLEntityNext extends PageMethod {
    protected SQLStore<?> sqlStore;

    SQLEntityNext(Class<?> returnType) {
        super(returnType);
    }

    @Override
    public void init() {
        super.init();
        this.sqlStore = SQLStoreFactory.build(sqlStoreService.getContext(), this.returnType, false);
    }

    protected Object doInvoke(String sql, Pagination pagination, Object[] arguments) {
        return sqlStoreService.next(readonly, sqlStore, sql, pagination, arguments);
    }
}
