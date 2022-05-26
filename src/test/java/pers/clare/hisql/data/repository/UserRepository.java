package pers.clare.hisql.data.repository;

import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.repository.SQLCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends SQLCrudRepository<User, Long> {
}
