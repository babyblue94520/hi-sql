package pers.clare.hisql.method;

import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreFactory;

public abstract class SQLStoreMethod extends SQLMethod {
    protected Class<?> valueType;
    protected SQLStore<?> sqlStore;

    SQLStoreMethod(Class<?> valueType) {
        this.valueType = valueType;
    }

    @Override
    public void init() {
        this.sqlStore = SQLStoreFactory.build(context, valueType, false);
        super.init();
    }
}
