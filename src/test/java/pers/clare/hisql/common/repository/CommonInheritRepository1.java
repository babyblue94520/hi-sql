package pers.clare.hisql.common.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.repository.SQLCrudRepository;

public interface CommonInheritRepository1<R, T, K, V> extends SQLCrudRepository<T, K> {
    @HiSql("INSERT INTO user (account)VALUES(:account)")
    Long insert(String account);

    @HiSql("SELECT * FROM user WHERE account=:account")
    CommonUser findByAccount(String account);

    @HiSql("UPDATE user SET name =:name WHERE id=:id")
    int update(long id, String name);
}
