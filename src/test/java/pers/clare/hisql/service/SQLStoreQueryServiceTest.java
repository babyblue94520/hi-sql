package pers.clare.hisql.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.vo.TestTable;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SQLStoreQueryServiceTest extends BasicTest {
    private final SQLStoreQueryService service;

    private final SQLCrudStore<TestTable> store;

    private final int max = 10;

    @Autowired
    public SQLStoreQueryServiceTest(SQLStoreQueryService service) {
        this.service = service;
        store = SQLStoreFactory.buildCrud(service.getNaming(), service.getResultSetConverter(), TestTable.class);
    }

    @Override
    protected int getMax() {
        return max;
    }

    @Test
    void find() {
        int total = max;
        TestTable testTable = service.find(store, findAll);
        assertEquals(1, testTable.getId());
        assertEquals("1", testTable.getName());
        testTable = service.find(store, findAll + " where id=?", 2);
        assertEquals(2, testTable.getId());
        assertEquals("2", testTable.getName());

        testTable = service.find(store, findAll, Sort.of(descColumn1));
        assertEquals(total, testTable.getId());
        assertEquals(String.valueOf(total), testTable.getName());
    }

    @Test
    void findSet() {
        int total = max;
        TestTable testTable = service.find(store, findAll);

        Set<TestTable> result = service.findSet(store, findAll);
        assertEquals(total, result.size());
        assertTrue(result.contains(testTable));

        total = max / 2;
        result = service.findSet(store, findAllWhereColumn1, total);
        assertEquals(total, result.size());
        assertFalse(result.contains(testTable));

        result = service.findSet(store, findAllWhereColumn1, Sort.of(descColumn1), total);
        for (TestTable table : result) {
            assertTrue(table.getId() > total);
        }
    }

    @Test
    void findAll() {
        int total = max;
        TestTable testTable = service.find(store, findAll);

        List<TestTable> result = service.findAll(store, findAll);
        assertEquals(total, result.size());
        assertTrue(result.contains(testTable));

        total = max / 2;
        result = service.findAll(store, findAllWhereColumn1, total);
        assertEquals(total, result.size());
        assertFalse(result.contains(testTable));

        result = service.findAll(store, findAllWhereColumn1, Sort.of(descColumn1), total);
        int prevId = Integer.MAX_VALUE;
        for (TestTable table : result) {
            assertTrue(prevId > table.getId());
            prevId = table.getId();
        }
    }

}
