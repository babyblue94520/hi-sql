package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.BasicTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SQLBasicServiceTest extends BasicTest {
    private final SQLService service;

    private final int max = 50;

    @Override
    protected int getMax() {
        return max;
    }

    @BeforeAll
    void build() {
        service.update("create table test2 (id int auto_increment, name varchar(255),primary key(id))");
        service.update("create table test3 (name varchar(255),primary key(name))");
    }

    @BeforeEach
    void truncate() {
        service.update("truncate table test2");
        service.update("truncate table test3");
    }
    
    @Test
    void connection() {
        String sql = "SELECT ?";
        Object value = "1";
        Object[] values = new Object[]{value};
        Object result = service.connection(sql, values, (connection, sql2, args) -> {
            assertEquals(sql, sql2);
            assertEquals(values, args);
            PreparedStatement ps = connection.prepareStatement(sql2);
            int i = 0;
            for (Object arg : args) {
                ps.setObject(++i, arg);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        });
        assertEquals(value, result);
    }

    @Test
    void prepared() {
        String sql = "SELECT ?";
        Object value = "1";
        Object result = service.prepared(sql, (ps) -> {
            ps.setObject(1, value);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        });
        assertEquals(value, result);
    }

    @Test
    void query() {
        String sql = "SELECT ?";
        Object value = "1";
        Object result = service.query(sql, new Object[]{value}, (rs) -> rs.next() ? rs.getObject(1) : null);
        assertEquals(value, result);

        sql = "SELECT '2'";
        result = service.query(sql, new Object[]{value}, (rs) -> rs.next() ? rs.getObject(1) : null);
        assertEquals("2", result);
    }


    @Test
    void insert() {
        int count = service.update("INSERT INTO test3 (name) VALUES ('test2')");
        assertEquals(1, count);
        long count2 = service.updateLarge("INSERT INTO test3 (name) VALUES ('test3')");
        assertEquals(1L, count2);

        int id = service.insert(Integer.class, "INSERT INTO test2 (name) VALUES ('test2')");
        assertEquals(1, id);
        for (int i = 1; i < max; i++) {
            id = service.insert(Integer.class, "INSERT INTO test2 (name) VALUES (?)", i);
            assertEquals(i + 1, id);
        }
        assertEquals(max, service.find(Integer.class, "SELECT COUNT(*) FROM test2"));

        int count3 = service.update("INSERT INTO test2 (name) VALUES ('test2')");
        assertEquals(1, count3);
        for (int i = 1; i < max; i++) {
            count3 = service.update("INSERT INTO test2 (name) VALUES (?)", i);
            assertEquals(1, count3);
        }
    }

    @Test
    void update() {
        for (int i = 0; i < max; i++) {
            assertEquals(1, service.update("INSERT INTO test2 (name) VALUES (?)", i));
        }
        String name = String.valueOf(System.currentTimeMillis());
        List<String> list = service.findAll(String.class, "SELECT name FROM test2");
        assertEquals(max, list.size());
        for (String s : list) {
            assertNotEquals(name, s);
        }
        assertEquals(max, service.update("UPDATE test2 SET name=?", name));
        list = service.findAll(String.class, "SELECT name FROM test2");
        assertEquals(max, list.size());
        for (String s : list) {
            assertEquals(name, s);
        }
    }

}
