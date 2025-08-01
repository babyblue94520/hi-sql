package pers.clare.hisql.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.util.SQLStoreFactory;
import pers.clare.hisql.vo.TestTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SQLStoreServiceTest extends BasicTest {
    private final SQLService service;

    private final SQLCrudStore<TestTable> store;

    private final int max = 10;

    @Autowired
    public SQLStoreServiceTest(SQLService service) {
        this.service = service;
        store = SQLStoreFactory.buildCrud(service, TestTable.class);
    }

    @Override
    protected int getMax() {
        return max;
    }

    @BeforeEach
    protected void create() {
        super.create();
        service.update("create table test (id int auto_increment, name varchar(255),primary key(id))");
    }

    @AfterEach
    protected void drop() {
        super.drop();
        service.update("drop table test");
    }

    @Test
    @Order(1)
    void insert() {
        String name = String.valueOf(System.currentTimeMillis());
        TestTable testTable = service.insert(store, new TestTable(null, name));
        assertNotNull(testTable.getId());
    }

    @Test
    @Order(2)
    void insertAll() {
        List<TestTable> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            list.add(new TestTable(null, String.valueOf(i + 1)));
        }
        service.insertAll(store, list);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
    }

    @Test
    @Order(3)
    void insertAll2() {
        TestTable[] array = new TestTable[max];
        for (int i = 0; i < max; i++) {
            array[i] = new TestTable(null, String.valueOf(i + 1));
        }
        service.insertAll(store, array);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
    }

    @Test
    @Order(4)
    void update() {
        String name = String.valueOf(System.currentTimeMillis());
        TestTable testTable = service.insert(store, new TestTable(null, name));
        assertNotNull(testTable.getId());
        String name2 = "test";
        service.update(store, new TestTable(testTable.getId(), name2));
        assertNotEquals(testTable.getName(), service.find(store, testTable).getName());
        assertEquals(name2, service.find(store, testTable).getName());
    }

    @Test
    @Order(5)
    void updateAll() {
        List<TestTable> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            list.add(new TestTable(null, String.valueOf(i + 1)));
        }
        service.insertAll(store, list);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
        String name = "test";
        for (TestTable testTable : list) {
            testTable.setName(name);
        }
        service.updateAll(store, list);
        for (TestTable testTable : service.findAll(store, store.getSelect())) {
            assertEquals(name, testTable.getName());
        }
    }

    @Test
    @Order(6)
    void updateAll2() {
        TestTable[] array = new TestTable[max];
        for (int i = 0; i < max; i++) {
            array[i] = new TestTable(null, String.valueOf(i + 1));
        }
        service.insertAll(store, array);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
        String name = "test";
        for (TestTable testTable : array) {
            testTable.setName(name);
        }
        service.updateAll(store, array);
        for (TestTable testTable : service.findAll(store, store.getSelect())) {
            assertEquals(name, testTable.getName());
        }
    }


    @Test
    @Order(7)
    void deleteAll() {
        List<TestTable> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            list.add(new TestTable(null, String.valueOf(i + 1)));
        }
        service.insertAll(store, list);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
        service.deleteAll(store, list.stream().filter(t -> t.getId() > max / 2).collect(Collectors.toList()));
        assertEquals(max / 2, service.find(Long.class, "SELECT COUNT(*) FROM test"));
    }

    @Test
    @Order(8)
    void deleteAll2() {
        TestTable[] array = new TestTable[max];
        for (int i = 0; i < max; i++) {
            array[i] = new TestTable(null, String.valueOf(i + 1));
        }
        service.insertAll(store, array);
        assertEquals(max, service.find(Long.class, "SELECT COUNT(*) FROM test"));
        service.deleteAll(store, Arrays.stream(array).filter(t -> t.getId() > max / 2).collect(Collectors.toList()));
        assertEquals(max / 2, service.find(Long.class, "SELECT COUNT(*) FROM test"));
    }


    @Test
    @Order(9)
    void byObject() {
        String name = String.valueOf(System.currentTimeMillis());
        TestTable testTable = service.insertByObject(new TestTable(null, name));
        assertNotNull(testTable.getId());
        assertNotNull(service.findByObject(testTable));
        String name2 = "test";
        service.updateByObject(new TestTable(testTable.getId(), name2));
        assertNotEquals(testTable.getName(), service.find(store, testTable).getName());
        assertEquals(name2, service.find(store, testTable).getName());
        service.deleteByObject(testTable);
        assertNull(service.findByObject(testTable));
    }

    @Test
    void find() {
        int total = max;
        TestTable testTable = service.find(store, findAll);
        assertEquals(1, testTable.getId());
        assertEquals("1", testTable.getName());
        testTable = service.find(store, findAll + " WHERE id=?", 2);
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

    @Test
    void page() {
        String sql = findAll;
        Page<TestTable> result = service.page(store, sql, (Pagination) null);
        int total = max;
        int page = 0;
        int count = 0;
        while (result.getRecords().size() > 0) {
            assertEquals(page++, result.getPage());
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
            assertEquals(service.getPagination(null).getSize(), result.getSize());
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
