package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.repository.SQLCrudRepository;

@Repository
public interface UserRepository extends SQLCrudRepository<User, Long> {
    @HiSql("insert into user (account)values(:account)")
    Long insert(String account);

    @HiSql("select * from user where account=:account")
    User findByAccount(String account);

    @HiSql("update user set name =:name where id=:id")
    int update(long id, String name);
}
