package pers.clare.hisql.method;

import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;

public abstract class SQLStoreMethod extends SQLMethod {
    protected SQLStore<?> sqlStore;

    SQLStoreMethod(Class<?> returnType) {
        super(returnType);
    }

    @Override
    public void init() {
        super.init();
        this.sqlStore = SQLStoreFactory.build(sqlStoreService.getContext(), this.returnType, false);
    }
}
