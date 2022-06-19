package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLServiceTest {
    private final SQLService service;

    private final int max = 10;

    @BeforeAll
    void build() {
        service.update("create table test2 (id int auto_increment, name varchar(255),primary key(id))");
    }

    @BeforeEach
    void truncate() {
        service.update("truncate table test2");
    }

    @Test
    void insert() {
        int id = service.insert(Integer.class, "insert into test2 (name) values ('test2')");
        assertEquals(1, id);
        for (int i = 1; i < max; i++) {
            id = service.insert(Integer.class, "insert into test2 (name) values (?)", i);
            assertEquals(i + 1, id);
        }
        assertEquals(max, service.find(Integer.class, "select count(*) from test2"));

        int count = service.insert("insert into test2 (name) values ('test2')");
        assertEquals(1, count);
        for (int i = 1; i < max; i++) {
            count = service.insert("insert into test2 (name) values (?)", i);
            assertEquals(1, count);
        }
    }

    @Test
    void update() {
        for (int i = 0; i < max; i++) {
            assertEquals(1, service.insert("insert into test2 (name) values (?)", i));
        }
        String name = String.valueOf(System.currentTimeMillis());
        List<String> list = service.findAll(String.class, "select name from test2");
        assertEquals(max, list.size());
        for (String s : list) {
            assertNotEquals(name, s);
        }
        assertEquals(max, service.update("update test2 set name=?", name));
        list = service.findAll(String.class, "select name from test2");
        assertEquals(max, list.size());
        for (String s : list) {
            assertEquals(name, s);
        }
    }
}
