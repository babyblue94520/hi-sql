package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.repository.SQLRepository;

@Repository
public interface InjectRepository extends SQLRepository {

    @HiSql("select true where 'test' = ?")
    Boolean query(String sql);

    @HiSql("select true where 'test' = :sql")
    Boolean query2(String sql);

    @HiSql("select :sql")
    String query3(String sql);
}
