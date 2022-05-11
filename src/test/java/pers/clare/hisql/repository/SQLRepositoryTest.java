package pers.clare.hisql.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.entity.User;
import pers.clare.hisql.data.repository.UserRepository;
import pers.clare.hisql.function.ResultSetCallback;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("SQLRepositoryTest")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class SQLRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void query() {
        User user = new User();
        String account = String.valueOf(System.currentTimeMillis());
        user.setAccount(account);
        assertNotNull(userRepository.insert(user));
        user = userRepository.query("select * from user where id=?", new Object[]{user.getId()}, resultSet -> {
            User query = null;
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<String, Integer> nameIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (int i = 1, l = metaData.getColumnCount(); i <= l; i++) {
                nameIndexMap.put(metaData.getColumnName(i), i);
            }
            if (resultSet.next()) {
                query = new User();
                query.setId(resultSet.getLong(nameIndexMap.get("id")));
                query.setAccount(resultSet.getString(nameIndexMap.get("account")));
                query.setCreateTime(resultSet.getLong(nameIndexMap.get("create_time")));
            }
            return query;
        });
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getAccount());
        assertEquals(0, user.getCreateTime());

    }

}
