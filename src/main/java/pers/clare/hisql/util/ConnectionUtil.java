package pers.clare.hisql.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;

public class ConnectionUtil {
    private static final Logger log = LogManager.getLogger();

    private ConnectionUtil() {
    }

    public static ResultSet query(Connection connection, String sql, Object[] parameters) throws SQLException {
        log.debug(sql);
        if (parameters.length == 0) {
            return connection.createStatement().executeQuery(sql);
        } else {
            PreparedStatement ps = connection.prepareStatement(sql);
            setQueryValue(ps, parameters);
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
            setUpdateValue(ps, parameters);
            return ps.executeUpdate();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
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

    public static void close(Connection connection, DataSource dataSource) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public static void setQueryValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0) return;
        for (Object value : parameters) {
            if (value instanceof Pagination || value instanceof Sort || value instanceof ResultSetCallback) continue;
            ps.setObject(index++, value);
        }
    }

    public static void setUpdateValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0) return;
        for (Object value : parameters) {
            if (value instanceof InputStream) {
                ps.setBinaryStream(index++, (InputStream) value);
            } else {
                ps.setObject(index++, value);
            }
        }
    }
}
