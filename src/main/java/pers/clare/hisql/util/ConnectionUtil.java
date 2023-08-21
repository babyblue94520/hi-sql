package pers.clare.hisql.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.io.InputStream;
import java.sql.*;

@SuppressWarnings("UnusedReturnValue")
public class ConnectionUtil {
    private static final Logger log = LogManager.getLogger();

    private ConnectionUtil() {
    }

    public static ResultSet query(Connection connection, String sql, Object[] parameters) throws SQLException {
        log.debug(sql);
        if (parameters == null || parameters.length == 0) {
            return connection.createStatement().executeQuery(sql);
        } else {
            PreparedStatement ps = connection.prepareStatement(sql);
            setQueryValue(ps, parameters);
            return ps.executeQuery();
        }
    }

    public static Statement insert(
            Connection conn
            , String sql
            , Object... parameters
    ) throws SQLException {
        log.debug(sql);
        if (parameters == null || parameters.length == 0) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return statement;
        } else {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setUpdateValue(ps, parameters);
            ps.executeUpdate();
            return ps;
        }
    }

    public static Statement update(
            Connection conn
            , String sql
            , Object... parameters
    ) throws SQLException {
        log.debug(sql);
        if (parameters == null || parameters.length == 0) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql);
            return statement;
        } else {
            PreparedStatement ps = conn.prepareStatement(sql);
            setUpdateValue(ps, parameters);
            ps.executeUpdate();
            return ps;
        }
    }

    public static void setQueryValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0 || ps.getParameterMetaData().getParameterCount() == 0) return;
        for (Object value : parameters) {
            if (value instanceof Pagination
                || value instanceof Sort
                || value instanceof ConnectionCallback
                || value instanceof PreparedStatementCallback
                || value instanceof ResultSetCallback
            ) continue;
            ps.setObject(index++, value);
        }
    }

    public static void setUpdateValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0 || ps.getParameterMetaData().getParameterCount() == 0) return;
        for (Object value : parameters) {
            if (value instanceof ConnectionCallback
                || value instanceof PreparedStatementCallback
                || value instanceof ResultSetCallback
            ) continue;
            if (value instanceof InputStream) {
                ps.setBinaryStream(index++, (InputStream) value);
            } else {
                ps.setObject(index++, value);
            }
        }
    }
}
