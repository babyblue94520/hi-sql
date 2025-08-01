package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Log4j2
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLTypeServiceTest extends BasicTest {
    private final SQLTypeService service;

    private final int max = 50;

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void find() {
        assertEquals(1L, service.find(Long.class, findAll));
        assertEquals("1", service.find(String.class, findAll));
        assertEquals("2", service.find(String.class, findAllWhereColumn1, 1));
        assertEquals("4", service.find(String.class, findAllWhereColumn1, 3));
        assertNull(service.find(String.class, findAllWhereColumn1, max));
    }

    @Test
    void findMap() {
        assertEquals(1L, service.findMap(Long.class, findAll).get(column1));
        assertEquals("1", service.findMap(String.class, findAll).get(column1));
        assertEquals("2", service.findMap(String.class, findAllWhereColumn1, 1).get(column1));

        assertEquals("1", service.findMap(String.class, findAll).get(column1));
        assertEquals("1", service.findMap(String.class, findAll).get(column2));
    }

    @Test
    void findSet() {
        String sql = findAll;
        assertTrue(service.findSet(Long.class, sql).contains(1L));
        assertEquals(max, service.findSet(Long.class, sql).size());
    }


    @Test
    void findAllMapSet() {
        assertEquals(max, service.findAllMapSet(Long.class, findAll).size());
    }

    @Test
    void findAllMap() {
        String sql = "SELECT 1,2 union all SELECT 2,3 union all SELECT 1,4 union all SELECT 2,3";
        assertEquals(4, service.findAllMap(Long.class, sql).size());
        sql = "SELECT 1,2 union all SELECT 2,3 union all SELECT 1,4 union all SELECT 2,3";
        assertEquals(4, service.findAllMap(Long.class, sql).size());
    }

    @Test
    void findAll() {
        String sql = findAll;
        assertEquals(max, service.findAll(Long.class, sql).size());
        assertEquals(max, service.findAll(String.class, sql).size());

        sql = "SELECT ?,? union all SELECT 3,4";
        List<Long> result = service.findAll(Long.class, sql, 1, 2);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(2, service.findAll(String.class, sql, 1, 2).size());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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


    @Test
    void pageFast() {
        String sql = findAll;
        long startTime = System.currentTimeMillis();
        Page<Long> result1 = service.page(Long.class, sql, Pagination.of(0, 20));
        long duration1 = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        Page<Long> result2 = service.page(Long.class, sql, Pagination.of(0, 20, max));
        long duration2 = System.currentTimeMillis() - startTime;
        log.info(duration1 + " : " + duration2);
        assertEquals(result1.getTotal(), result2.getTotal());
    }
}
