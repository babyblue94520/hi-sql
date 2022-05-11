package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.repository.SQLCrudRepository;

@Repository
public interface UserCallbackRepository extends SQLCrudRepository<User> {

    @HiSql("update user set name = :name where id=:id")
    User updateName(Long id, String name, ConnectionCallback<User> callback);
}
