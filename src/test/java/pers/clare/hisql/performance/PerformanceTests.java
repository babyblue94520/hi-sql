package pers.clare.hisql.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserRepository;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.performance.jpa.UserJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Log4j2
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(args = "--logging.level.pers.clare=info")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PerformanceTests {

    private final UserJpaRepository userJpaRepository;

    private final UserRepository userRepository;

    private final int thread = Runtime.getRuntime().availableProcessors();
    private final int max = 100000;

    private final int pageSize = 100;

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

    @Test
    @Order(1)
    void jpa_insert() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                userJpaRepository.save(createUser());
            }
            count.decrementAndGet();
            return null;
        });
    }

    @Test
    @Order(2)
    void jpa_select() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        org.springframework.data.domain.Page<User> page = userJpaRepository.findAll(PageRequest.of(0, pageSize));
        assertEquals(page.getContent().size(), pageSize);
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                User user = page.getContent().get(count.get() % pageSize);
                assertNotNull(userJpaRepository.findById(user.getId()));
            }
            count.decrementAndGet();
            return null;
        });
    }


    @Test
    @Order(3)
    void jpa_update() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        org.springframework.data.domain.Page<User> page = userJpaRepository.findAll(PageRequest.of(0, pageSize));
        List<User> users = page.getContent();
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                User user = users.get(count.get() % pageSize);
                user.setUpdateTime(System.currentTimeMillis());
                userJpaRepository.save(user);
            }
            count.decrementAndGet();
            return null;
        });
    }


    @Test
    @Order(1)
    void hisql_insert() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                userRepository.insert(createUser());
            }
            count.decrementAndGet();
            return null;
        });
    }

    @Test
    @Order(2)
    void hisql_select() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        assertEquals(page.getRecords().size(), pageSize);
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                User user = page.getRecords().get(count.get() % pageSize);
                assertNotNull(userRepository.findById(user.getId()));
            }
            count.decrementAndGet();
            return null;
        });
    }

    @Test
    @Order(3)
    void hisql_update() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        List<User> users = page.getRecords();
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                User user = users.get(count.get() % pageSize);
                user.setUpdateTime(System.currentTimeMillis());
                userRepository.update(user);
            }
            count.decrementAndGet();
            return null;
        });
    }

    @Test
    @Order(3)
    void hisql_update2() throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        Page<User> page = userRepository.page(Pagination.of(0, pageSize));
        List<User> users = page.getRecords();
        performance(thread, () -> {
            while (count.incrementAndGet() <= max) {
                User user = users.get(count.get() % pageSize);
                userRepository.update(user.getId(), System.currentTimeMillis());
            }
            count.decrementAndGet();
            return null;
        });
    }

    private void performance(int thread, Callable<Void> callable) throws InterruptedException, ExecutionException {
        long t = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < thread; i++) {
            tasks.add(callable);
        }
        for (Future<Void> future : executorService.invokeAll(tasks)) {
            future.get();
        }
        long ms = System.currentTimeMillis() - t;
        if (ms == 0) {
            ms = 1;
        }
        log.info("time: {} , tps: {}", ms, max * 1000 / ms);
        executorService.shutdown();
    }


}
