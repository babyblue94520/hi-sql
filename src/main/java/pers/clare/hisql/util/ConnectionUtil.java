package pers.clare.hisql.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.support.ConnectionReuseHolder;

import java.sql.*;

public class ConnectionUtil {
    private static final Logger log = LogManager.getLogger();

    ConnectionUtil() {
    }



    public static ResultSet query(Connection connection, String sql, Object[] parameters) throws SQLException {
        log.debug(sql);
        if (parameters.length == 0) {
            return connection.createStatement().executeQuery(sql);
        } else {
            PreparedStatement ps = connection.prepareStatement(sql);
            SQLUtil.setValue(ps, parameters);
            return ps.executeQuery();
        }
    }

    public static ResultSet query(Statement statement, String sql) throws SQLException {
        log.debug(sql);
        return statement.executeQuery(sql);
    }

    public static Statement insert(
            Connection conn
            , String sql
            , Object... parameters
    ) throws SQLException {
        log.debug(sql);
        if (parameters.length == 0) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return statement;
        } else {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object value : parameters) {
                ps.setObject(i++, value);
            }
            ps.executeUpdate();
            return ps;
        }
    }

    public static int update(
            Connection conn
            , String sql
            , Object... parameters
    ) throws SQLException {
        log.debug(sql);
        if (parameters.length == 0) {
            return conn.createStatement().executeUpdate(sql);
        } else {
            PreparedStatement ps = conn.prepareStatement(sql);
            SQLUtil.setValue(ps, parameters);
            return ps.executeUpdate();
        }
    }

    public static int insert(
            Statement statement
            , String sql
    ) throws SQLException {
        log.debug(sql);
        return statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public static int update(
            Statement statement
            , String sql
    ) throws SQLException {
        log.debug(sql);
        return statement.executeUpdate(sql);
    }

    public static void close(Connection connection) {
        if (connection == null) return;
        try {
            if (ConnectionReuseHolder.get() == null) {
                connection.close();
            }
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

}
