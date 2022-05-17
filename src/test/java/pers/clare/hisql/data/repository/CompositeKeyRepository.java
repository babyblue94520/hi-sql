package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.CompositeKey;
import pers.clare.hisql.data.entity.CompositeTable;
import pers.clare.hisql.repository.SQLCrudRepository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CompositeKeyRepository extends SQLCrudRepository<CompositeTable, CompositeKey> {

    @HiSql("insert into composite_table (account)values(:account)")
    Long insert(String account);

    @HiSql("select * from composite_table where (id,account) in :keys")
    List<CompositeTable> findAll(Collection<CompositeKey> keys);

    @HiSql("select * from composite_table where (id,account) in :keys")
    List<CompositeTable> findAll(CompositeKey[] keys);
}
