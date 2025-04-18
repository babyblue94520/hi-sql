package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.CompositeKey;
import pers.clare.hisql.data.entity.CompositeTable;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserRepository;
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
public class SQLCrudRepositoryTest {

    private final UserRepository userRepository;

    @BeforeEach
    void before() {
        userRepository.deleteAll();
    }

    User create() {
        User user = new User();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        userRepository.insert(user);
        user = userRepository.findById(user.getId());
        assertNotNull(user);
        assertEquals(account, user.getAccount());
        return user;
    }

    @Test
    void count() {
        assertEquals(0, userRepository.count());
        create();
        assertEquals(1, userRepository.count());
        create();
        assertEquals(2, userRepository.count());
        User user = create();
        assertEquals(1, userRepository.count(user));
    }

    @Test
    void countById() {
        User user = create();
        assertEquals(1, userRepository.countById(user.getId()));
    }

    @Test
    void findAll() {
        assertEquals(0, userRepository.findAll().size());
        create();
        assertEquals(1, userRepository.findAll().size());
        create();
        assertEquals(2, userRepository.findAll().size());
        List<User> users = userRepository.findAll(Sort.of("id desc,account asc"));
        assertTrue(users.get(0).getId() > users.get(users.size() - 1).getId());
        users = userRepository.findAll(Sort.of("id asc,account desc"));
        assertTrue(users.get(0).getId() < users.get(users.size() - 1).getId());
    }

    @Test
    void page() {
        int page = 0;
        int size = 5;
        Page<User> userPage = userRepository.page(Pagination.of(page, size));
        assertEquals(0, userPage.getTotal());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((userPage = userRepository.page(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += userPage.getRecords().size();
            assertEquals(size, userPage.getSize());
            assertEquals(total, userPage.getTotal());
        }
        assertEquals(total, count);

        List<User> users = userRepository.page(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(users.get(0).getId() > users.get(users.size() - 1).getId());
        users = userRepository.page(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(users.get(0).getId() < users.get(users.size() - 1).getId());
    }

    @Test
    void next() {
        int page = 0;
        int size = 5;
        Next<User> userNext = userRepository.next(Pagination.of(page, size));
        assertEquals(0, userNext.getRecords().size());
        int total = 23;
        for (int i = 0; i < total; i++) {
            create();
        }
        int count = 0;
        while ((userNext = userRepository.next(Pagination.of(page++, size))).getRecords().size() > 0) {
            count += userNext.getRecords().size();
            assertEquals(size, userNext.getSize());
        }
        assertEquals(total, count);

        List<User> users = userRepository.next(Pagination.of(0, size, "id desc,account asc")).getRecords();
        assertTrue(users.get(0).getId() > users.get(users.size() - 1).getId());
        users = userRepository.next(Pagination.of(0, size, "id asc,account desc")).getRecords();
        assertTrue(users.get(0).getId() < users.get(users.size() - 1).getId());
    }

    @Test
    void find() {
        User user = create();
        User user2 = userRepository.find(user);
        assertNotNull(user2);
        assertEquals(user.getId(), user2.getId());
        assertEquals(user.getAccount(), user2.getAccount());
    }

    @Test
    void findById() {
        User user = create();
        User user2 = userRepository.findById(user.getId());
        assertNotNull(user2);
        assertEquals(user.getId(), user2.getId());
        assertEquals(user.getAccount(), user2.getAccount());
    }

    @Test
    void findAllByIds() {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(create());
        }
        Long[] keys = new Long[list.size()];
        for (int i = 0; i < 10; i++) {
            var data = list.get(i);
            keys[i] = data.getId();
        }
        List<User> result = userRepository.findAllByIds(keys);
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
        User user = create();
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
        assertEquals(1, user.getCount());
        assertEquals(0, user.getUpdateUser());
        assertEquals(0, user.getUpdateTime());
        assertEquals(0, user.getCreateUser());
        assertEquals(0, user.getCreateTime());
        assertTrue(user.getEnabled());
        assertFalse(user.getLocked());
    }

    @Test
    void update() {
        User user = create();
        String name = String.valueOf(System.currentTimeMillis());
        user.setName(name);
        int count = userRepository.update(user);
        assertEquals(1, count);
        assertEquals(name, userRepository.findById(user.getId()).getName());
    }

    @Test
    void delete() {
        User user = create();
        int count = userRepository.delete(user);
        assertEquals(1, count);
        assertNull(userRepository.find(user));
    }

    @Test
    void deleteById() {
        User user = create();
        int count = userRepository.deleteById(user.getId());
        assertEquals(1, count);
        assertNull(userRepository.findById(user.getId()));
    }

    @Test
    void deleteByIds() {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(create());
        }
        Long[] keys = new Long[list.size()];
        for (int i = 0; i < 10; i++) {
            var data = list.get(i);
            keys[i] = data.getId();
        }
        int count = userRepository.deleteByIds(keys);
        assertEquals(list.size(), count);
        for (var entity : list) {
            assertNull(userRepository.find(entity));
        }
    }

    @Test
    void insertAll() {
        int count = 10;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setAccount(String.valueOf(System.currentTimeMillis()));
            users.add(user);
        }
        Collection<User> users2 = userRepository.insertAll(users);
        for (User user : users2) {
            User user2 = userRepository.find(user);
            assertNotNull(user2);
            assertEquals(user.getId(), user2.getId());
            assertEquals(user.getAccount(), user2.getAccount());
        }
    }

    @Test
    void insertAllArray() {
        int count = 10;
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setAccount(String.valueOf(System.currentTimeMillis()));
            users[i] = user;
        }
        users = userRepository.insertAll(users);
        for (User user : users) {
            User user2 = userRepository.find(user);
            assertNotNull(user2);
            assertEquals(user.getId(), user2.getId());
            assertEquals(user.getAccount(), user2.getAccount());
        }
    }

    @Test
    void updateAll() {
        int count = 10;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(create());
        }
        long updateTime = System.currentTimeMillis();
        for (User user : users) {
            user.setUpdateTime(updateTime);
        }

        for (int i : userRepository.updateAll(users)) {
            assertEquals(1, i);
        }
        for (User user : users) {
            assertEquals(updateTime, userRepository.find(user).getUpdateTime());
        }
    }

    @Test
    void updateAllArray() {
        int count = 10;
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = create();
        }
        long updateTime = System.currentTimeMillis();
        for (User user : users) {
            user.setUpdateTime(updateTime);
        }

        for (int i : userRepository.updateAll(users)) {
            assertEquals(1, i);
        }
        for (User user : users) {
            assertEquals(updateTime, userRepository.find(user).getUpdateTime());
        }
    }

    @Test
    void deleteAll() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            create();
        }
        assertEquals(count, userRepository.count());
        int result = userRepository.deleteAll();
        assertEquals(count, result);
        assertEquals(0, userRepository.count());
    }


    @Test
    void deleteAllCollection() {
        int count = 10;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(create());
        }
        assertEquals(count, userRepository.count());
        for (int i : userRepository.deleteAll(users)) {
            assertEquals(1, i);
        }
        assertEquals(0, userRepository.count());
    }

    @Test
    void deleteAllArray() {
        int count = 10;
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = create();
        }
        assertEquals(count, userRepository.count());
        for (int i : userRepository.deleteAll(users)) {
            assertEquals(1, i);
        }
        assertEquals(0, userRepository.count());
    }

    @Test
    void byObject() {
        User user = new User();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        userRepository.insertByObject(user);
        assertNotNull(userRepository.findByObject(user));
        assertEquals(account, user.getAccount());

        String name = String.valueOf(System.currentTimeMillis());
        user.setName(name);
        assertEquals(1, userRepository.updateByObject(user));
        assertEquals(name, userRepository.findByObject(user).getName());
        assertEquals(1, userRepository.deleteByObject(user));
        assertNull(userRepository.findByObject(user));
    }
}
