package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.data.entity.CompositeKey;
import pers.clare.hisql.data.entity.CompositeKey2;
import pers.clare.hisql.data.entity.CompositeTable;
import pers.clare.hisql.repository.SQLCrudRepository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CompositeKeyRepository extends SQLCrudRepository<CompositeTable, CompositeKey> {

    @HiSql(value = "insert into composite_table (account)values(:account)", returnIncrementKey = true)
    Long insert(String account);

    @HiSql("select * from composite_table where (id,account) in :keys")
    List<CompositeTable> findAll(Collection<CompositeKey> keys);

    @HiSql("select * from composite_table where (account,id) in :keys")
    List<CompositeTable> findAll2(Collection<CompositeKey2> keys);

    @HiSql("select * from composite_table where (id,account) in :keys")
    List<CompositeTable> findAll(CompositeKey[] keys);

    @HiSql("select * from composite_table where (account,id) in :keys")
    List<CompositeTable> findAll2(CompositeKey2[] keys);
}
