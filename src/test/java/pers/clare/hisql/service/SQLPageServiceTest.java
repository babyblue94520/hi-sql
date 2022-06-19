package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLPageServiceTest extends BasicTest {
    private final SQLPageService service;

    private final int max = 50;

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void page() {
        String sql = findAll;
        Page<Long> result = service.page(Long.class, sql);
        int total = max;
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            count += result.getRecords().size();
            result = service.page(Long.class, sql, Pagination.of(page, result.getSize()));
        }
        assertEquals(total, count);
    }

    @Test
    void page2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Page<Long> result = service.page(Long.class, sql, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            result = service.page(Long.class, sql, Pagination.of(page, result.getSize()), total);
        }
        assertEquals(total, count);
    }

    @Test
    void page3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Page<Long> result = service.page(Long.class, sql, sort, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            count += result.getRecords().size();
            long prev = Integer.MAX_VALUE;
            for (Long record : result.getRecords()) {
                assertTrue(record < prev);
                prev = record;
            }
            result = service.page(Long.class, sql, Pagination.of(page, result.getSize(), sort), total);
        }
        assertEquals(total, count);
    }

    @Test
    void pageMap() {
        String sql = findAll;
        Page<Map<String, Long>> result = service.pageMap(Long.class, sql);
        int total = max;
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            result.getRecords().forEach(map -> assertEquals(map.get(column1), map.get(column2)));
            assertEquals(total, result.getTotal());
            count += result.getRecords().size();
            result = service.pageMap(Long.class, sql, Pagination.of(page, result.getSize()));
        }
        assertEquals(total, count);
    }

    @Test
    void pageMap2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Page<Map<String, Long>> result = service.pageMap(Long.class, sql, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            result.getRecords().forEach(map -> assertEquals(map.get(column1), map.get(column2)));
            count += result.getRecords().size();
            result = service.pageMap(Long.class, sql, Pagination.of(page, result.getSize()), total);
        }
        assertEquals(total, count);
    }

    @Test
    void pageMap3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Page<Map<String, Long>> result = service.pageMap(Long.class, sql, sort, total);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLPageService.DefaultPagination.getSize(), result.getSize());
            assertEquals(total, result.getTotal());
            long prev = Integer.MAX_VALUE;
            for (Map<String, Long> map : result.getRecords()) {
                assertEquals(map.get(column1), map.get(column2));
                assertTrue(map.get(column1) < prev);
                prev = map.get(column1);
            }
            count += result.getRecords().size();
            result = service.pageMap(Long.class, sql, Pagination.of(page, result.getSize(), sort), total);
        }
        assertEquals(total, count);
    }
}
