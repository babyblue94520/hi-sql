package pers.clare.hisql.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("UserRepositoryTest")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void insert() {
        long prevId = 0;
        for (int i = 0; i < 5; i++) {
            String account = String.valueOf(System.currentTimeMillis());
            Long id = userRepository.insert(account);
            assertNotNull(userRepository.findById(id));
            assertTrue(id > prevId);
            prevId = id;
        }
    }

}
