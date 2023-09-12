package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.repository.InjectRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InjectRepositoryTest {

    private final InjectRepository injectRepository;

    void verify(String sql) {
        assertNull(injectRepository.query(sql));
        assertNull(injectRepository.query2(sql));
        // H2 does not handle '\'.
        assertEquals(sql.replaceAll("\\\\", "\\\\\\\\"), injectRepository.query3(sql));
    }

    @Test
    void query() {
        String sql = "\\' or '1'='1'";

        verify(sql);
    }

    @Test
    void query2() {
        String sql = "' or '1\'='1'";

        verify(sql);
    }

    @Test
    void query3() {
        String sql = "' or '1\\'='1'";

        verify(sql);
    }

    @Test
    void query4() {
        String sql = " \u001A¥\n\r₩";
        verify(sql);
        System.out.println(injectRepository.query3(sql));
    }

    public static void main(String[] args) {
        System.out.println("\\".replaceAll("\\\\", "\\\\\\\\"));
    }
}
