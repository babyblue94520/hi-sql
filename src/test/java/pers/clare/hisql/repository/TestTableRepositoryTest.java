package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.TestTable;
import pers.clare.hisql.data.entity.TestTableKey;
import pers.clare.hisql.data.repository.TestTableRepository;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestTableRepositoryTest {

    private final TestTableRepository testTableRepository;

    TestTable create() {
        TestTable data = new TestTable();
        String account = String.valueOf(System.currentTimeMillis());
        data.setAccount(account);
        testTableRepository.insert(data);
        return testTableRepository.find(data);
    }

    @Test
    void count() {
        testTableRepository.deleteAll();
        assertEquals(0, testTableRepository.count());
        create();
        assertEquals(1, testTableRepository.count());
        create();
        assertEquals(2, testTableRepository.count());
        TestTable data = create();
        assertEquals(1, testTableRepository.count(data));
    }

    @Test
    void countById() {
        TestTable data = create();

        assertEquals(1
                , testTableRepository.countById(
                        new TestTableKey()
                                .setId(data.getId())
                                .setAccount(data.getAccount())
                )
        );
    }

    @Test
    void findAll() {
        testTableRepository.deleteAll();
        assertEquals(0, testTableRepository.findAll().size());
        create();
        assertEquals(1, testTableRepository.findAll().size());
        create();
        assertEquals(2, testTableRepository.findAll().size());
        List<TestTable> datas = testTableRepository.findAll(Sort.of("id desc,account asc"));
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = testTableRepository.findAll(Sort.of("id asc,account desc"));
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void page() {
        testTableRepository.deleteAll();
        int page = 0;
        int size = 5;
        Page<TestTable> dataPage = testTableRepository.page(Pagination.of(page, size));
        assertEquals(0, dataPage.getTotal());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((dataPage = testTableRepository.page(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += dataPage.getRecords().size();
            assertEquals(size, dataPage.getSize());
            assertEquals(total, dataPage.getTotal());
        }
        assertEquals(total, count);

        List<TestTable> datas = testTableRepository.page(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = testTableRepository.page(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void next() {
        testTableRepository.deleteAll();
        int page = 0;
        int size = 5;
        Next<TestTable> dataNext = testTableRepository.next(Pagination.of(page, size));
        assertEquals(0, dataNext.getRecords().size());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((dataNext = testTableRepository.next(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += dataNext.getRecords().size();
            assertEquals(size, dataNext.getSize());
        }
        assertEquals(total, count);

        List<TestTable> datas = testTableRepository.next(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = testTableRepository.next(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void find() {
        TestTable data = create();
        TestTable data2 = testTableRepository.find(data);
        assertNotNull(data2);
        assertEquals(data.getId(), data2.getId());
        assertEquals(data.getAccount(), data2.getAccount());
    }

    @Test
    void findById() {
        TestTable data = create();
        TestTable data2 = testTableRepository.findById(
                new TestTableKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        );
        assertNotNull(data2);
        assertEquals(data.getId(), data2.getId());
        assertEquals(data.getAccount(), data2.getAccount());
    }

    @Test
    void insert() {
        TestTable data = create();
        assertEquals("", data.getName());
    }

    @Test
    void update() {
        TestTable data = create();
        String name = String.valueOf(System.currentTimeMillis());
        data.setName(name);
        int count = testTableRepository.update(data);
        assertEquals(1, count);
        assertEquals(name, testTableRepository.findById(
                new TestTableKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        ).getName());
    }

    @Test
    void delete() {
        TestTable data = create();
        int count = testTableRepository.delete(data);
        assertEquals(1, count);
        assertNull(testTableRepository.find(data));
    }

    @Test
    void deleteById() {
        TestTable data = create();
        int count = testTableRepository.deleteById(
                new TestTableKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        );
        assertEquals(1, count);
        assertNull(testTableRepository.findById(
                new TestTableKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        ));
    }

    @Test
    void insertAll() {
        int count = 10;
        List<TestTable> datas = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TestTable data = new TestTable();
            data.setAccount(String.valueOf(System.currentTimeMillis()));
            datas.add(data);
        }
        Collection<TestTable> datas2 = testTableRepository.insertAll(datas);
        for (TestTable data : datas2) {
            TestTable data2 = testTableRepository.find(data);
            assertNotNull(data2);
            assertEquals(data.getId(), data2.getId());
            assertEquals(data.getAccount(), data2.getAccount());
        }
    }

    @Test
    void insertAllArray() {
        int count = 10;
        TestTable[] datas = new TestTable[count];
        for (int i = 0; i < count; i++) {
            TestTable data = new TestTable();
            data.setAccount(String.valueOf(System.currentTimeMillis()));
            datas[i] = data;
        }
        datas = testTableRepository.insertAll(datas);
        for (TestTable data : datas) {
            TestTable data2 = testTableRepository.find(data);
            assertNotNull(data2);
            assertEquals(data.getId(), data2.getId());
            assertEquals(data.getAccount(), data2.getAccount());
        }
    }

    @Test
    void updateAllArray() {
        int count = 10;
        TestTable[] datas = new TestTable[count];
        for (int i = 0; i < count; i++) {
            datas[i] = create();
        }
        String updateTime = String.valueOf(System.currentTimeMillis());
        for (TestTable data : datas) {
            data.setName(updateTime);
        }

        for (int i : testTableRepository.updateAll(datas)) {
            assertEquals(1, i);
        }
        for (TestTable data : datas) {
            assertEquals(updateTime, testTableRepository.find(data).getName());
        }
    }

    @Test
    void deleteAll() {
        testTableRepository.deleteAll();
        int count = 10;
        for (int i = 0; i < count; i++) {
            create();
        }
        assertEquals(count, testTableRepository.count());
        int result = testTableRepository.deleteAll();
        assertEquals(count, result);
        assertEquals(0, testTableRepository.count());
    }
}
