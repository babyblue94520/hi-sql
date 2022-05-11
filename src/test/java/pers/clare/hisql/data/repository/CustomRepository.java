package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.repository.SQLRepository;

@Repository
public interface CustomRepository extends SQLRepository {

}
