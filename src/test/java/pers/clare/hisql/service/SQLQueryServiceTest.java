package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLQueryServiceTest extends BasicTest {
    private final SQLQueryService service;

    private final int max = 5;

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
        String sql = "select 1,2 union all select 2,3 union all select 1,4 union all select 2,3";
        assertEquals(4, service.findAllMap(Long.class, sql).size());
        sql = "select 1,2 union all select 2,3 union all select 1,4 union all select 2,3";
        assertEquals(4, service.findAllMap(Long.class, sql).size());
    }

    @Test
    void findAll() {
        String sql = findAll;
        assertEquals(max, service.findAll(Long.class, sql).size());
        assertEquals(max, service.findAll(String.class, sql).size());

        sql = "select ?,? union all select 3,4";
        List<Long> result = service.findAll(Long.class, sql, 1, 2);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(2, service.findAll(String.class, sql, 1, 2).size());
    }
}
