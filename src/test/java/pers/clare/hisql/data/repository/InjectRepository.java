package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.repository.SQLRepository;

@Repository
public interface InjectRepository extends SQLRepository {

    @HiSql("SELECT true WHERE 'test' = ?")
    Boolean query(String sql);

    @HiSql("SELECT true WHERE 'test' = :sql")
    Boolean query2(String sql);

    @HiSql("SELECT :sql")
    String query3(String sql);
}
