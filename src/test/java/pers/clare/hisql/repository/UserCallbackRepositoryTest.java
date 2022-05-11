package pers.clare.hisql.repository;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserCallbackRepository;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class UserCallbackRepositoryTest {

    @Autowired
    private UserCallbackRepository repository;

    User buildUser() {
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
        User result = repository.updateName(user.getId(), "Test", (connection, sql, parameters) -> {
            Statement statement = connection.createStatement();
            if (statement.executeUpdate(sql) > 0) {
                return repository.findById(parameters[0]);
            }else{
                return null;
            }
        });
        assertEquals("Test", result.getName());
    }


}
