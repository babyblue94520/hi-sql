package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.data.entity.TestTable;
import pers.clare.hisql.data.entity.TestTableKey;
import pers.clare.hisql.repository.SQLCrudRepository;

@Repository
public interface TestTableRepository extends SQLCrudRepository<TestTable, TestTableKey> {
}
