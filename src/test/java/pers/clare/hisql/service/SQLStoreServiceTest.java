package pers.clare.hisql.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.vo.TestTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SQLStoreServiceTest {
    private final SQLStoreService service;

    private final SQLCrudStore<TestTable> store;

    private final int max = 10;

    @Autowired
    public SQLStoreServiceTest(SQLStoreService service) {
        this.service = service;
        store = SQLStoreFactory.build(service.getContext(), TestTable.class, true);
    }

    @BeforeEach
    protected void create() {
        service.update("create table test (id int auto_increment, name varchar(255),primary key(id))");
    }

    @AfterEach
    protected void drop() {
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
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
    }

    @Test
    @Order(3)
    void insertAll2() {
        TestTable[] array = new TestTable[max];
        for (int i = 0; i < max; i++) {
            array[i] = new TestTable(null, String.valueOf(i + 1));
        }
        service.insertAll(store, array);
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
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
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
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
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
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
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
        service.deleteAll(store, list.stream().filter(t -> t.getId() > max / 2).collect(Collectors.toList()));
        assertEquals(max / 2, service.find(Long.class, "select count(*) from test"));
    }

    @Test
    @Order(8)
    void deleteAll2() {
        TestTable[] array = new TestTable[max];
        for (int i = 0; i < max; i++) {
            array[i] = new TestTable(null, String.valueOf(i + 1));
        }
        service.insertAll(store, array);
        assertEquals(max, service.find(Long.class, "select count(*) from test"));
        service.deleteAll(store, Arrays.stream(array).filter(t -> t.getId() > max / 2).collect(Collectors.toList()));
        assertEquals(max / 2, service.find(Long.class, "select count(*) from test"));
    }
}
