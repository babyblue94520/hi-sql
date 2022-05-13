package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTest {

    private final UserRepository userRepository;

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
