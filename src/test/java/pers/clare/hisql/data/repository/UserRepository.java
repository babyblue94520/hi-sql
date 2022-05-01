package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;

@Repository
public interface UserRepository extends SQLCrudRepository<User>{
    @HiSql("insert into user (account)values(:account)")
    void insert(String account);

    @HiSql("select * from user where account=:account")
    User findByAccount(String account);

    @HiSql("update user set name =:name where id=:id")
    int update(long id, String name);
}
