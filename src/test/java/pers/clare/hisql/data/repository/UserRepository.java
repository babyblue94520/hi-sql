package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.repository.SQLCrudRepository;

@Repository
public interface UserRepository extends SQLCrudRepository<User, Long> {

}
