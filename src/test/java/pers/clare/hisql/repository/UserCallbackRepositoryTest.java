package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserCallbackRepository;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserCallbackRepositoryTest {

    private final UserCallbackRepository repository;

    private User buildUser() {
        User user = new User();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        repository.insert(user);
        user = repository.findById(user.getId());
        assertNotNull(user);
        assertEquals(account, user.getAccount());
        return user;
    }

    @Test
    void connection() {
        User user = buildUser();
        User result = repository.update(user.getId(), "Test", (connection, sql, parameters) -> {
            Statement statement = connection.createStatement();
            if (statement.executeUpdate(sql) > 0) {
                return repository.findById(user.getId());
            } else {
                return null;
            }
        });
        assertEquals("Test", result.getName());
    }

    @Test
    void update() {
        User user = buildUser();
        repository.update(user.getId(), "Test");
        user = repository.findById(user.getId());
        assertEquals("1", user.getName());
    }

}
