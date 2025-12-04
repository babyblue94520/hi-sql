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

    @HiSql(value = "INSERT INTO composite_table (account)VALUES(:account)", returnIncrementKey = true)
    Long insert(String account);

    @HiSql("SELECT * FROM composite_table WHERE (id,account) IN :keys")
    List<CompositeTable> findAll(Collection<CompositeKey> keys);


    @HiSql("SELECT * FROM composite_table WHERE (id,account) IN :keys")
    List<CompositeTable> findAll(CompositeKey[] keys);
}
