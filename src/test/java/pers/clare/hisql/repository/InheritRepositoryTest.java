package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.data.repository.InheritRepository1;
import pers.clare.hisql.data.repository.InheritRepository2;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InheritRepositoryTest {

    private final InheritRepository1 inheritRepository1;

    private final InheritRepository2 inheritRepository2;

    CommonUser buildUser() {
        CommonUser user = new CommonUser();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        inheritRepository1.insert(user);
        user = inheritRepository1.findById(user.getId()).orElse(null);
        assertNotNull(user);
        assertEquals(account, user.getAccount());
        return user;
    }

    @Test
    void connection() {
        CommonUser user = buildUser();
        CommonUser result = inheritRepository1.update(user.getId(), "Test", (connection, sql, parameters) -> {
            Statement statement = connection.createStatement();
            if (statement.executeUpdate(sql) > 0) {
                return inheritRepository1.findById(user.getId()).orElse(null);
            } else {
                return null;
            }
        });
        assertEquals("Test", result.getName());
    }

    @Test
    void update() {
        CommonUser user = buildUser();
        inheritRepository1.update(user.getId(), "Test");
        user = inheritRepository1.findById(user.getId()).orElse(new CommonUser());
        assertEquals("1", user.getName());
    }

    @Test
    void update2() {
        CommonUser user = buildUser();
        inheritRepository2.update(user.getId(), "Test");
        user = inheritRepository2.findById(user.getId()).orElse(new CommonUser());
        assertEquals("1", user.getName());
    }
}
