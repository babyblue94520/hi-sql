package pers.clare.hisql.performance;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import pers.clare.h2.H2Application;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserRepository;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.performance.jpa.UserJpaRepository;
import pers.clare.hisql.util.PerformanceUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Log4j2
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(args = {"--logging.level.pers.clare=info", "--spring.profiles.active=h2remote"})
public class PerformanceTests {

    static {
        SpringApplication.run(H2Application.class
                , "--spring.profiles.active=h2server"
                , "--h2.port=3390"
        );
    }

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final int thread = Runtime.getRuntime().availableProcessors();

    private final int pageSize = 100;

    private final long time = 5000;

    User createUser() {
        long time = System.currentTimeMillis();
        return new User()
                .setAccount(String.valueOf(time))
                .setName("")
                .setEmail("")
                .setCount(0)
                .setLocked(false)
                .setEnabled(true)
                .setUpdateTime(time)
                .setUpdateUser(0L)
                .setCreateTime(time)
                .setCreateUser(0L);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        if (userRepository.count() == 0) {
            hisql_insert();
        }
    }

    @Test
    @Order(1)
    void jpa_insert() throws Exception {
        PerformanceUtil.byTime("jpa_insert", thread, time, (index) -> {
            userJpaRepository.save(createUser());
        });
    }

    @Test
    @Order(2)
    void jpa_select() throws Exception {
        org.springframework.data.domain.Page<User> page = userJpaRepository.findAll(PageRequest.of(0, pageSize));
        assertEquals(page.getContent().size(), pageSize);
        PerformanceUtil.byTime("jpa_select", thread, time, (index) -> {
            User user = page.getContent().get((int) (index % pageSize));
            assertNotNull(userJpaRepository.findById(user.getId()));
        });
    }


    @Test
    @Order(4)
    void jpa_update() throws Exception {
        org.springframework.data.domain.Page<User> page = userJpaRepository.findAll(PageRequest.of(0, pageSize));
        List<User> users = page.getContent();
        PerformanceUtil.byTime("jpa_update", thread, time, (index) -> {
            User user = users.get((int) (index % pageSize));
            user.setUpdateTime(System.currentTimeMillis());
            userJpaRepository.save(user);
        });
    }


    @Test
    @Order(1)
    void hisql_insert() throws Exception {
        PerformanceUtil.byTime("hisql_insert", thread, time, (index) -> {
            userRepository.insert(createUser());
        });
    }

    @Test
    @Order(2)
    void hisql_select() throws Exception {
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        assertEquals(page.getRecords().size(), pageSize);
        PerformanceUtil.byTime("hisql_select", thread, time, (index) -> {
            User user = page.getRecords().get((int) (index % pageSize));
            assertNotNull(userRepository.findById(user.getId()));
        });
    }

    @Test
    @Order(3)
    void hisql_update() throws Exception {
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        List<User> users = page.getRecords();
        PerformanceUtil.byTime("hisql_update", thread, time, (index) -> {
            User user = users.get((int) (index % pageSize));
            user.setUpdateTime(System.currentTimeMillis());
            userRepository.update(user);
        });
    }

    @Test
    @Order(3)
    void hisql_update2() throws Exception {
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        List<User> users = page.getRecords();
        PerformanceUtil.byTime("hisql_update2", thread, time, (index) -> {
            User user = users.get((int) (index % pageSize));
            userRepository.update(user.getId(), System.currentTimeMillis());
        });
    }

    @Test
    @Order(4)
    void jdbc_update() throws Exception {
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        List<User> users = page.getRecords();
        PerformanceUtil.byTime("jdbc_update", thread, time, (index) -> {
            User user = users.get((int) (index % pageSize));
            jdbcTemplate.update("update user set update_time = ? where id=?", System.currentTimeMillis(), user.getId());
        });
    }
}
