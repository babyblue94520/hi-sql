package pers.clare.hisql.function;


import pers.clare.hisql.service.SQLStoreService;

@FunctionalInterface
public interface SqlInvoke {
    Object apply(SQLStoreService service, String sql, Object[] arguments);
}
