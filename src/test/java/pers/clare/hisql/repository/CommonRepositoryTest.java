package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.common.data.CommonUser;
import pers.clare.hisql.data.repository.CommonRepositoryImpl;
import pers.clare.hisql.data.repository.CommonRepositoryImpl2;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor
public class CommonRepositoryTest {

    @Autowired
    private CommonRepositoryImpl commonRepositoryImpl;

    @Autowired
    private CommonRepositoryImpl2 commonRepositoryImpl2;

    CommonUser buildUser() {
        CommonUser user = new CommonUser();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        commonRepositoryImpl.insert(user);
        user = commonRepositoryImpl.findById(user.getId());
        assertNotNull(user);
        assertEquals(account, user.getAccount());
        return user;
    }

    @Test
    void connection() {
        CommonUser user = buildUser();
        CommonUser result = commonRepositoryImpl.update(user.getId(), "Test", (connection, sql, parameters) -> {
            Statement statement = connection.createStatement();
            if (statement.executeUpdate(sql) > 0) {
                return commonRepositoryImpl.findById(parameters[0]);
            } else {
                return null;
            }
        });
        assertEquals("Test", result.getName());
    }

    @Test
    void update() {
        CommonUser user = buildUser();
        commonRepositoryImpl.update(user.getId(), "Test");
        user = commonRepositoryImpl.findById(user.getId());
        assertEquals("1", user.getName());
    }

    @Test
    void update2() {
        CommonUser user = buildUser();
        commonRepositoryImpl2.update(user.getId(), "Test");
        user = commonRepositoryImpl2.findById(user.getId());
        assertEquals("1", user.getName());
    }
}
