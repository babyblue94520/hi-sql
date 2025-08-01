package pers.clare.hisql.function;


import pers.clare.hisql.service.SQLService;

@FunctionalInterface
public interface SqlInvoke {
    Object apply(SQLService service, String sql, Object[] arguments, Object[] originArguments);
}
