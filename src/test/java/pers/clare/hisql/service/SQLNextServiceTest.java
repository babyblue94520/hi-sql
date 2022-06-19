package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLNextServiceTest extends BasicTest {
    private final SQLNextService service;

    private final int max = 50;

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void next() {
        String sql = findAll;
        Next<Long> result = service.next(Long.class, sql);
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            count += result.getRecords().size();
            result = service.next(Long.class, sql, Pagination.of(page, result.getSize()));
        }
        assertEquals(max, count);
    }

    @Test
    void next2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Next<Long> result = service.next(Long.class, sql, total);
        int page = 0;
        int count = 0;
        do {
            result = service.next(Long.class, sql, Pagination.of(page, result.getSize()), total);
            count += result.getRecords().size();
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
        } while (result.getRecords().size() > 0);
        assertEquals(total, count);
    }

    @Test
    void next3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Next<Long> result = service.next(Long.class, sql, sort, total);
        int page = 0;
        int count = 0;
        do {
            result = service.next(Long.class, sql, Pagination.of(page, result.getSize(), sort), total);
            count += result.getRecords().size();
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            long prev = Integer.MAX_VALUE;
            for (Long record : result.getRecords()) {
                assertTrue(record < prev);
                prev = record;
            }
        } while (result.getRecords().size() > 0);
        assertEquals(total, count);
    }

    @Test
    void nextMap() {
        String sql = findAll;
        Next<Map<String, Long>> result = service.nextMap(Long.class, sql);
        int page = 0;
        int count = 0;
        do {
            result = service.nextMap(Long.class, sql, Pagination.of(page, result.getSize()));
            count += result.getRecords().size();
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            result.getRecords().forEach(map -> assertEquals(map.get(column1), map.get(column2)));
        } while (result.getRecords().size() > 0);
        assertEquals(max, count);
    }

    @Test
    void nextMap2() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Next<Map<String, Long>> result = service.nextMap(Long.class, sql, total);
        int page = 0;
        int count = 0;
        do {
            result = service.nextMap(Long.class, sql, Pagination.of(page, result.getSize()), total);
            count += result.getRecords().size();
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            result.getRecords().forEach(map -> assertEquals(map.get(column1), map.get(column2)));
        } while (result.getRecords().size() > 0);
        assertEquals(total, count);
    }

    @Test
    void nextMap3() {
        String sql = findAllWhereColumn1;
        int total = max / 2;
        Sort sort = Sort.of(descColumn1);
        Next<Map<String, Long>> result = service.nextMap(Long.class, sql, sort, total);
        int page = 0;
        int count = 0;
        do {
            result = service.nextMap(Long.class, sql, Pagination.of(page, result.getSize(), sort), total);
            count += result.getRecords().size();
            assertEquals(page++, result.getPage());
            assertEquals(SQLNextService.DefaultPagination.getSize(), result.getSize());
            long prev = Integer.MAX_VALUE;
            for (Map<String, Long> map : result.getRecords()) {
                assertEquals(map.get(column1), map.get(column2));
                assertTrue(map.get(column1) < prev);
                prev = map.get(column1);
            }
        } while (result.getRecords().size() > 0);
        assertEquals(total, count);
    }
}
