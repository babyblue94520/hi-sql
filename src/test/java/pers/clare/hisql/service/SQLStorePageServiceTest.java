package pers.clare.hisql.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;
import pers.clare.hisql.page.Page;
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
class SQLStorePageServiceTest extends BasicTest {
    private final SQLStorePageService service;

    private final int max = 50;

    private final SQLCrudStore<TestTable> store;

    @Autowired
    public SQLStorePageServiceTest(SQLStorePageService service) {
        this.service = service;
        store = SQLStoreFactory.build(service.getNaming(),service.getResultSetConverter(), TestTable.class, true);
    }

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void page() {
        String sql = findAll;
        Page<TestTable> result = service.page(store, sql, (Pagination) null);
        int total = max;
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            count += result.getRecords().size();
            result = service.page(store, sql, Pagination.of(page, result.getSize()));
        }
        assertEquals(total, count);
    }

    @Test
    void page2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Page<TestTable> result = service.page(store, sql, (Pagination) null, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            result = service.page(store, sql, Pagination.of(page, result.getSize()), total);
        }
        assertEquals(total, count);
    }

    @Test
    void page3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Page<TestTable> result = service.page(store, sql, sort, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            count += result.getRecords().size();
            long prev = Integer.MAX_VALUE;
            for (TestTable record : result.getRecords()) {
                assertTrue(record.getId() < prev);
                prev = record.getId();
            }
            result = service.page(store, sql, Pagination.of(page, result.getSize(), sort), total);
        }
        assertEquals(total, count);
    }
}
