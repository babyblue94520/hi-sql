package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.CompositeKey;
import pers.clare.hisql.data.entity.CompositeKey2;
import pers.clare.hisql.data.entity.CompositeTable;
import pers.clare.hisql.data.repository.CompositeKeyRepository;
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
public class CompositeKeyRepositoryTest {

    private final CompositeKeyRepository compositeKeyRepository;

    CompositeTable create() {
        CompositeTable data = new CompositeTable();
        String account = String.valueOf(System.currentTimeMillis());
        data.setAccount(account);
        compositeKeyRepository.insert(data);
        return compositeKeyRepository.find(data);
    }

    @BeforeEach
    void before() {
        compositeKeyRepository.deleteAll();
    }

    @Test
    void count() {
        assertEquals(0, compositeKeyRepository.count());
        create();
        assertEquals(1, compositeKeyRepository.count());
        create();
        assertEquals(2, compositeKeyRepository.count());
        CompositeTable data = create();
        assertEquals(1, compositeKeyRepository.count(data));
    }

    @Test
    void countById() {
        CompositeTable data = create();

        assertEquals(1
                , compositeKeyRepository.countById(
                        new CompositeKey()
                                .setId(data.getId())
                                .setAccount(data.getAccount())
                )
        );
    }

    @Test
    void findAll() {
        assertEquals(0, compositeKeyRepository.findAll().size());
        create();
        assertEquals(1, compositeKeyRepository.findAll().size());
        create();
        assertEquals(2, compositeKeyRepository.findAll().size());
        List<CompositeTable> datas = compositeKeyRepository.findAll(Sort.of("id desc,account asc"));
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = compositeKeyRepository.findAll(Sort.of("id asc,account desc"));
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void page() {
        int page = 0;
        int size = 5;
        Page<CompositeTable> dataPage = compositeKeyRepository.page(Pagination.of(page, size));
        assertEquals(0, dataPage.getTotal());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((dataPage = compositeKeyRepository.page(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += dataPage.getRecords().size();
            assertEquals(size, dataPage.getSize());
            assertEquals(total, dataPage.getTotal());
        }
        assertEquals(total, count);

        List<CompositeTable> datas = compositeKeyRepository.page(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = compositeKeyRepository.page(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void next() {
        int page = 0;
        int size = 5;
        Next<CompositeTable> dataNext = compositeKeyRepository.next(Pagination.of(page, size));
        assertEquals(0, dataNext.getRecords().size());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((dataNext = compositeKeyRepository.next(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += dataNext.getRecords().size();
            assertEquals(size, dataNext.getSize());
        }
        assertEquals(total, count);

        List<CompositeTable> datas = compositeKeyRepository.next(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(datas.get(0).getId() > datas.get(datas.size() - 1).getId());
        datas = compositeKeyRepository.next(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(datas.get(0).getId() < datas.get(datas.size() - 1).getId());
    }

    @Test
    void find() {
        CompositeTable data = create();
        CompositeTable data2 = compositeKeyRepository.find(data);
        assertNotNull(data2);
        assertEquals(data.getId(), data2.getId());
        assertEquals(data.getAccount(), data2.getAccount());
    }

    @Test
    void findById() {
        CompositeTable data = create();
        CompositeTable result = compositeKeyRepository.findById(
                new CompositeKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        );
        assertNotNull(result);
        assertEquals(data.getId(), result.getId());
        assertEquals(data.getAccount(), result.getAccount());
    }

    @Test
    void findAllByIds() {
        List<CompositeTable> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(create());
        }
        CompositeKey[] keys = new CompositeKey[list.size()];
        for (int i = 0; i < 10; i++) {
            var data = list.get(i);
            keys[i] = new CompositeKey()
                    .setId(data.getId())
                    .setAccount(data.getAccount());
        }
        List<CompositeTable> result = compositeKeyRepository.findAllByIds(keys);
        assertNotNull(result);
        for (int i = 0; i < result.size(); i++) {
            var o = list.get(i);
            var r = result.get(i);
            assertEquals(o.getId(), r.getId());
            assertEquals(o.getAccount(), r.getAccount());
        }
    }

    @Test
    void insert() {
        CompositeTable data = create();
        assertEquals("", data.getName());
    }

    @Test
    void update() {
        CompositeTable data = create();
        String name = String.valueOf(System.currentTimeMillis());
        data.setName(name);
        int count = compositeKeyRepository.update(data);
        assertEquals(1, count);
        var entity = compositeKeyRepository.findById(
                new CompositeKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        );
        assertNotNull(entity);
        assertEquals(name, entity.getName());
    }

    @Test
    void delete() {
        CompositeTable data = create();
        int count = compositeKeyRepository.delete(data);
        assertEquals(1, count);
        assertNull(compositeKeyRepository.find(data));
    }

    @Test
    void deleteById() {
        CompositeTable data = create();
        int count = compositeKeyRepository.deleteById(
                new CompositeKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        );
        assertEquals(1, count);
        assertNull(compositeKeyRepository.findById(
                new CompositeKey()
                        .setId(data.getId())
                        .setAccount(data.getAccount())
        ));
    }

    @Test
    void deleteByIds() {
        List<CompositeTable> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(create());
        }
        CompositeKey[] keys = new CompositeKey[list.size()];
        for (int i = 0; i < 10; i++) {
            var data = list.get(i);
            keys[i] = new CompositeKey()
                    .setId(data.getId())
                    .setAccount(data.getAccount());
        }
        int count = compositeKeyRepository.deleteByIds(keys);
        assertEquals(list.size(), count);
        for (var entity : list) {
            assertNull(compositeKeyRepository.find(entity));
        }
    }

    @Test
    void insertAll() {
        int count = 10;
        List<CompositeTable> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CompositeTable data = new CompositeTable();
            data.setAccount(String.valueOf(System.currentTimeMillis()));
            list.add(data);
        }
        Collection<CompositeTable> list2 = compositeKeyRepository.insertAll(list);
        for (CompositeTable data : list2) {
            CompositeTable data2 = compositeKeyRepository.find(data);
            assertNotNull(data2);
            assertEquals(data.getId(), data2.getId());
            assertEquals(data.getAccount(), data2.getAccount());
        }
    }

    @Test
    void insertAllArray() {
        int count = 10;
        CompositeTable[] datas = new CompositeTable[count];
        for (int i = 0; i < count; i++) {
            CompositeTable data = new CompositeTable();
            data.setAccount(String.valueOf(System.currentTimeMillis()));
            datas[i] = data;
        }
        datas = compositeKeyRepository.insertAll(datas);
        for (CompositeTable data : datas) {
            CompositeTable data2 = compositeKeyRepository.find(data);
            assertNotNull(data2);
            assertEquals(data.getId(), data2.getId());
            assertEquals(data.getAccount(), data2.getAccount());
        }
    }

    @Test
    void updateAllArray() {
        int count = 10;
        CompositeTable[] records = new CompositeTable[count];
        for (int i = 0; i < count; i++) {
            records[i] = create();
        }
        String updateTime = String.valueOf(System.currentTimeMillis());
        for (CompositeTable data : records) {
            data.setName(updateTime);
        }

        for (int i : compositeKeyRepository.updateAll(records)) {
            assertEquals(1, i);
        }
        for (CompositeTable data : records) {
            var entity = compositeKeyRepository.find(data);
            assertNotNull(entity);
            assertEquals(updateTime, entity.getName());
        }
    }

    @Test
    void deleteAll() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            create();
        }
        assertEquals(count, compositeKeyRepository.count());
        int result = compositeKeyRepository.deleteAll();
        assertEquals(count, result);
        assertEquals(0, compositeKeyRepository.count());
    }

    @Test
    void whereInMoreArray() {
        int count = 5;
        int total = 0;
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            compositeKeyRepository.insert(account);
        }

        CompositeKey[] values = new CompositeKey[count];
        CompositeKey2[] values2 = new CompositeKey2[count];
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            CompositeKey key = new CompositeKey(compositeKeyRepository.insert(account), account);
            values[i] = key;
            values2[i] = new CompositeKey2(key.getId(), key.getAccount());
        }
        assertEquals(total, compositeKeyRepository.count());
        List<CompositeTable> users = compositeKeyRepository.findAll(values);
        assertEquals(count, users.size());
        users = compositeKeyRepository.findAll2(values2);
        assertEquals(count, users.size());
    }

    @Test
    void whereInMoreCollection() {
        int count = 5;
        int total = 0;
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            compositeKeyRepository.insert(account);
        }

        List<CompositeKey> values = new ArrayList<>();
        List<CompositeKey2> values2 = new ArrayList<>();
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            CompositeKey key = new CompositeKey(compositeKeyRepository.insert(account), account);
            values.add(key);
            values2.add(new CompositeKey2(key.getId(), key.getAccount()));
        }
        assertEquals(total, compositeKeyRepository.count());
        List<CompositeTable> users = compositeKeyRepository.findAll(values);
        assertEquals(count, users.size());
        users = compositeKeyRepository.findAll2(values2);
        assertEquals(count, users.size());
    }
}
