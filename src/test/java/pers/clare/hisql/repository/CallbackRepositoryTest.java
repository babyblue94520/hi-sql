package pers.clare.hisql.repository;

import pers.clare.hisql.data.repository.CallbackRepository;
import pers.clare.hisql.util.ConnectionUtil;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CallbackRepositoryTest {

    private final CallbackRepository callbackRepository;

    @Test
    void connection() {
        long value = 1L;
        var result = callbackRepository.connection(1L, (connection, sql, parameters) -> {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

    @Test
    void connectionPrepared() {
        long value = 1L;
        long result = callbackRepository.connectionPrepared(1L, (connection, sql, parameters) -> {
            PreparedStatement prepareStatement = connection.prepareStatement(sql);
            ConnectionUtil.setQueryValue(prepareStatement, parameters);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

    @Test
    void prepared() {
        long value = 1L;
        long result = callbackRepository.prepared(value, (preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

    @Test
    void prepared2() {
        long value = 1L;
        long result = callbackRepository.prepared2((preparedStatement) -> {
            preparedStatement.setLong(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

    @Test
    void resultSet() {
        long value = 1L;
        long result = callbackRepository.resultSet(value, (resultSet) -> {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

}
