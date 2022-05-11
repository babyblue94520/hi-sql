package pers.clare.hisql.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.repository.CallbackRepository;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class CallbackRepositoryTest {

    @Autowired
    private CallbackRepository callbackRepository;

    @Test
    void connection() {
        long value = 1L;
        long result = callbackRepository.connection(1L, (connection, sql, parameters) -> {
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
        long result = callbackRepository.prepared(1L, (preparedStatement, parameters) -> {
            ConnectionUtil.setQueryValue(preparedStatement, parameters);
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
        long result = callbackRepository.prepared2(1L, (preparedStatement, parameters) -> {
            ConnectionUtil.setQueryValue(preparedStatement, parameters);
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
    void prepared3() {
        String value = "1";
        String result = callbackRepository.prepared3((preparedStatement, parameters) -> {
            ConnectionUtil.setQueryValue(preparedStatement, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return String.valueOf(resultSet.getLong(1));
            } else {
                return String.valueOf(0);
            }
        });
        assertEquals("1", result);
    }

    @Test
    void resultSet() {
        long value = 1L;
        long result = callbackRepository.resultSet(1L, (resultSet) -> {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0L;
            }
        });
        assertEquals(value, result);
    }

}
