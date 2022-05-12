package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.function.ConnectionCallback;

@Repository
public interface UserCallbackRepository extends UserRepository {
    @HiSql("update user set name = :name where id=:id")
    User update(Long id, String name, ConnectionCallback<User> callback);

    @HiSql("update user set name =1 where id=:id")
    int update(long id, String name);
}
