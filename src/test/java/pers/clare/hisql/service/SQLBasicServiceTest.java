package pers.clare.hisql.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SQLBasicServiceTest {
    private final SQLBasicService sqlBasicService;

    @Test
    void connection() {
        String sql = "select ?";
        Object value = "1";
        Object[] values = new Object[]{value};
        Object result = sqlBasicService.connection(sql, values, (connection, sql2, args) -> {
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
        String sql = "select ?";
        Object value = "1";
        Object result = sqlBasicService.prepared(sql, (ps) -> {
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
        String sql = "select ?";
        Object value = "1";
        Object result = sqlBasicService.query(sql, new Object[]{value}, (rs) -> rs.next() ? rs.getObject(1) : null);
        assertEquals(value, result);

        sql = "select '2'";
        result = sqlBasicService.query(sql, new Object[]{value}, (rs) -> rs.next() ? rs.getObject(1) : null);
        assertEquals("2", result);
    }
}
