package pers.clare.hisql.service;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.JdbcUtils;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.service.impl.SQLServiceImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SQLBasicServiceCleanupTest {
    private JdbcConnectionPool pool;
    private SQLServiceImpl service;

    @BeforeEach
    void setUp() {
        pool = JdbcConnectionPool.create("jdbc:h2:mem:cleanup-" + UUID.randomUUID(), "sa", "");
        pool.setMaxConnections(1);
        pool.setLoginTimeout(1);
        service = new SQLServiceImpl();
        service.setDataSource(pool);
    }

    @AfterEach
    void tearDown() {
        pool.dispose();
    }

    @Test
    void repeatedNativeQueriesCloseAllResourcesAndReturnTheOnlyPoolConnection() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            Connection connection = pool.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT 1");
            try {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt(1)).isEqualTo(1);
                service.closeAll("SELECT 1", connection, resultSet);
                assertThat(resultSet.isClosed()).isTrue();
                assertThat(statement.isClosed()).isTrue();
                assertThat(connection.isClosed()).isTrue();
                assertThat(pool.getActiveConnections()).isZero();
            } finally {
                JdbcUtils.closeResultSet(resultSet);
                JdbcUtils.closeStatement(statement);
                JdbcUtils.closeConnection(connection);
            }
        }
    }

    @Test
    void failureToReadStatementStillReturnsTheConnection() throws Exception {
        Connection connection = pool.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT 1");
        resultSet.close();
        try {
            assertThatThrownBy(() -> service.closeAll("SELECT 1", connection, resultSet))
                    .isInstanceOf(HiSqlException.class);
            assertThat(connection.isClosed()).isTrue();
            assertThat(pool.getActiveConnections()).isZero();
        } finally {
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }
    }

    @Test
    void absentResultSetStillReturnsTheConnection() throws Exception {
        Connection connection = pool.getConnection();
        try {
            service.closeAll("UPDATE tx_probe SET id = 1", connection, null);
            assertThat(connection.isClosed()).isTrue();
            assertThat(pool.getActiveConnections()).isZero();
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }
}
