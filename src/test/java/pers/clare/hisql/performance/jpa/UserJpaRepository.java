package pers.clare.hisql.performance.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import pers.clare.hisql.data.entity.User;

public interface UserJpaRepository extends PagingAndSortingRepository<User, Long> {
}
