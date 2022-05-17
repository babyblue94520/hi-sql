package pers.clare.hisql.common.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.repository.SQLCrudRepository;

public interface CommonInheritRepository1<R, T, K, V> extends SQLCrudRepository<T, K> {
    @HiSql("insert into user (account)values(:account)")
    Long insert(String account);

    @HiSql("select * from user where account=:account")
    CommonUser findByAccount(String account);

    @HiSql("update user set name =:name where id=:id")
    int update(long id, String name);
}
