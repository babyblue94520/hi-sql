package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.repository.SQLCrudRepository;

@Repository
public interface UserRepository extends SQLCrudRepository<User, Long> {

    @HiSql("UPDATE user SET update_time = :updateTime WHERE id=:id")
    int update(Long id, Long updateTime);
}
