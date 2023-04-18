package pers.clare.hisql.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.vo.TestTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SQLStoreNextServiceTest extends BasicTest {
    private final SQLStoreNextService service;

    private final int max = 50;

    private final SQLCrudStore<TestTable> store;

    @Autowired
    public SQLStoreNextServiceTest(SQLStoreNextService service) {
        this.service = service;
        store = SQLStoreFactory.build(service.getNaming(), service.getResultSetConverter(), TestTable.class, true);
    }

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void next() {
        String sql = findAll;
        Next<TestTable> result = service.next(store, sql);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            result = service.next(store, sql, Pagination.of(page, result.getSize()));
        }
        assertEquals(max, count);
    }

    @Test
    void next2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Next<TestTable> result = service.next(store, sql, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            result = service.next(store, sql, Pagination.of(page, result.getSize()), total);
        }
        assertEquals(total, count);
    }

    @Test
    void next3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Next<TestTable> result = service.next(store, sql, sort, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            long prev = Integer.MAX_VALUE;
            for (TestTable record : result.getRecords()) {
                assertTrue(record.getId() < prev);
                prev = record.getId();
            }
            result = service.next(store, sql, Pagination.of(page, result.getSize(), sort), total);
        }
        assertEquals(total, count);
    }
}
