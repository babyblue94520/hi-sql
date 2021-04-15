package pers.clare.hisql.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class ConnectionCache {
    private final DataSource dataSource;
    private final boolean transaction;
    private final int isolation;
    private Connection connection;
    private Boolean autocommit = null;
    private Integer transactionIsolation = null;

    public ConnectionCache(
            boolean transaction
            , int isolation
            , DataSource dataSource
    ) {
        this.transaction = transaction;
        this.isolation = isolation;
        this.dataSource = dataSource;
    }

    public Connection open() throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
            if (transaction && connection.getAutoCommit()) {
                autocommit = true;
                connection.setAutoCommit(false);
            }
            if (isolation != Connection.TRANSACTION_NONE && isolation != connection.getTransactionIsolation()) {
                transactionIsolation = connection.getTransactionIsolation();
                connection.setTransactionIsolation(isolation);
            }
        }
        return connection;
    }

    public void rollback() throws SQLException {
        if (autocommit != null) {
            connection.rollback();
        }
    }

    public void commit() throws SQLException {
        if (autocommit != null) {
            connection.commit();
        }
    }

    public void close() throws SQLException {
        if (connection == null) return;
        if (transactionIsolation != null) {
            connection.setTransactionIsolation(transactionIsolation);
        }
        if (autocommit != null) {
            connection.setAutoCommit(true);
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        connection = null;
    }
}
