package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.entity.UserSimple;
import pers.clare.hisql.data.repository.CustomRepository;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.support.SqlReplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SQLRepositoryTest {

    private final CustomRepository customRepository;

    User build() {
        String account = String.valueOf(System.currentTimeMillis());
        Long id = customRepository.insert(account);
        User user = customRepository.findById(id);
        assertNotNull(user);
        return user;
    }

    @BeforeEach
    void before() {
        customRepository.delete();
    }

    @Test
    void insert() {
        long prevId = 0;
        for (int i = 0; i < 5; i++) {
            User user = build();
            assertNotNull(customRepository.findById(user.getId()));
            assertTrue(user.getId() > prevId);
            prevId = user.getId();
        }
    }

    @Test
    void update() {
        User user = build();
        String name = String.valueOf(System.currentTimeMillis());
        customRepository.update(user.getId(), name);
        user = customRepository.findById(user.getId());
        assertEquals(name, user.getName());
    }

    @Test
    void updateByUser() {
        User user = build();
        String name = String.valueOf(System.currentTimeMillis());
        user.setName(name);
        customRepository.update(user);
        user = customRepository.findById(user.getId());
        assertEquals(name, user.getName());
    }

    @Test
    void deleteByUser() {
        User user = build();
        String name = String.valueOf(System.currentTimeMillis());
        user.setName(name);
        customRepository.delete(user);
        user = customRepository.findById(user.getId());
        assertNull(user);
    }

    @Test
    void findByAccount() {
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < 5; i++) {
            customRepository.insert(account);
        }
        List<User> users = customRepository.findAllByAccount(account);
        assertEquals(5, users.size());
        assertNotNull(customRepository.findByAccount(account));
    }

    @Test
    void findSimpleById() {
        User user = build();
        UserSimple userSimple = customRepository.findSimpleById(user.getId());
        assertNotNull(userSimple);
        assertEquals(userSimple.getId(), user.getId());
        assertEquals(userSimple.getAccount(), user.getAccount());
        assertEquals(userSimple.getName(), user.getName());
        assertEquals(userSimple.getEmail(), user.getEmail());
    }

    @Test
    void findMapById() {
        User user = build();
        Map<String, Object> map = customRepository.findMapById(user.getId());
        assertNotNull(map);
        assertEquals(map.get("ID"), user.getId());
        assertEquals(map.get("ACCOUNT"), user.getAccount());
        assertEquals(map.get("NAME"), user.getName());
        assertEquals(map.get("EMAIL"), user.getEmail());
    }

    @Test
    void findAllSet() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        List<UserSimple> users = customRepository.findAll();
        assertEquals(count, users.size());
        Set<UserSimple> userSet = customRepository.findAllSet();
        assertEquals(1, userSet.size());
    }

    @Test
    void findAllMapSet() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        List<UserSimple> users = customRepository.findAll();
        assertEquals(count, users.size());
        Set<Map<String, Object>> userSet = customRepository.findAllMapSet();
        assertEquals(count, userSet.size());
    }

    @Test
    void pageByAccount() {
        Page<User> page = customRepository.pageByAccount("");
        assertNotNull(page);
        assertNotNull(page.getRecords());
        assertEquals(0, page.getTotal());
        customRepository.delete();
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        page = customRepository.pageByAccount(account);
        assertEquals(count, page.getRecords().size());
        assertEquals(count, page.getTotal());
    }

    @Test
    void pageByAccount2() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        Pagination pagination = Pagination.of(0, 3);
        Page<User> page = customRepository.pageByAccount(pagination, account);
        assertEquals(pagination.getSize(), page.getRecords().size());
        assertEquals(count, page.getTotal());

        pagination = Pagination.of(3, 3);
        page = customRepository.pageByAccount(pagination, account);
        assertEquals(0, page.getRecords().size());
        assertEquals(count, page.getTotal());
    }


    @Test
    void nextByAccount() {
        Next<User> next = customRepository.nextByAccount("");
        assertNotNull(next);
        assertNotNull(next.getRecords());
        customRepository.delete();
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        next = customRepository.nextByAccount(account);
        assertEquals(count, next.getRecords().size());
    }

    @Test
    void nextByAccount2() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        Pagination pagination = Pagination.of(0, 3);
        Next<User> next = customRepository.nextByAccount(pagination, account);

        assertEquals(pagination.getSize(), next.getRecords().size());
        pagination = Pagination.of(3, 3);
        next = customRepository.nextByAccount(pagination, account);
        assertEquals(0, next.getRecords().size());
    }

    @Test
    void sort() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            customRepository.insert(account);
        }
        List<User> users = customRepository.findAllByAccount(Sort.of("id desc,account asc"), account);
        long prevId = Long.MAX_VALUE;
        for (User user : users) {
            assertTrue(prevId > user.getId());
            prevId = user.getId();
        }
        users = customRepository.findAllByAccount(Sort.of("id asc,account asc"), account);
        prevId = 0;
        for (User user : users) {
            assertTrue(prevId < user.getId());
            prevId = user.getId();
        }
    }

    @Test
    void whereInArray() {
        int count = 5;
        int total = 0;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++, total++) {
            customRepository.insert(account);
        }
        Long[] ids = new Long[count];
        for (int i = 0; i < count; i++, total++) {
            ids[i] = customRepository.insert(account);
        }
        assertEquals(total, customRepository.count());
        List<User> users = customRepository.findAll(ids, account);
        assertEquals(count, users.size());
    }

    @Test
    void whereInCollection() {
        int count = 5;
        int total = 0;
        String account = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < count; i++, total++) {
            customRepository.insert(account);
        }
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++, total++) {
            ids.add(customRepository.insert(account));
        }
        assertEquals(total, customRepository.count());
        List<User> users = customRepository.findAll(ids, account);
        assertEquals(count, users.size());

    }

    @Test
    void whereInMoreArray() {
        int count = 5;
        int total = 0;
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            customRepository.insert(account);
        }

        Object[][] values = new Object[count][2];
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            values[i] = new Object[]{customRepository.insert(account), account};
        }
        assertEquals(total, customRepository.count());
        List<User> users = customRepository.findAll(values);
        assertEquals(count, users.size());
    }

    @Test
    void whereInMoreCollection() {
        int count = 5;
        int total = 0;
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            customRepository.insert(account);
        }

        List<Object[]> values = new ArrayList<>();
        for (int i = 0; i < count; i++, total++) {
            String account = String.valueOf(System.currentTimeMillis());
            values.add(new Object[]{customRepository.insert(account), account});
        }
        assertEquals(total, customRepository.count());
        List<User> users = customRepository.findAll(values);
        assertEquals(count, users.size());
    }

    @Test
    void whereInThrow() {
        String account = "";
        Long[] array = new Long[0];
        assertThrows(IllegalArgumentException.class, () -> customRepository.findAll(array, account));
        List<Long> list = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> customRepository.findAll(list, account));

        Long[] nullArray = null;
        assertThrows(HiSqlException.class, () -> customRepository.findAll(nullArray, account));
        List<Long> nullList = null;
        assertThrows(HiSqlException.class, () -> customRepository.findAll(nullList, account));
    }

    @Test
    void findAllByReplaceSql() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        Long id = null;
        for (int i = 0; i < count; i++) {
            id = customRepository.insert(account);
        }
        List<User> users = customRepository.findAll(
                ""
                , id
        );
        assertEquals(count, users.size());
        users = customRepository.findAll(
                "and id=:id"
                , id
        );
        assertEquals(1, users.size());
    }


    @Test
    void findAllBySqlReplace() {
        int count = 5;
        String account = String.valueOf(System.currentTimeMillis());
        Long id = null;
        for (int i = 0; i < count; i++) {
            id = customRepository.insert(account);
        }

        List<User> users = customRepository.findAll(SqlReplace.of(id, ""));
        assertEquals(count, users.size());
        users = customRepository.findAll(SqlReplace.of(id, "and id=:id"));
        assertEquals(1, users.size());
    }
}
