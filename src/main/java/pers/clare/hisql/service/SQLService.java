package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.util.ConnectionUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLService extends SQLPageService {

    public SQLService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    public int insert(
            String sql
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            Statement statement = ConnectionUtil.insert(connection, sql, parameters);
            return statement.getUpdateCount();
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> T insert(
            Class<T> keyType
            , String sql
            , Object... parameters
    ) {
        if (keyType == null) throw new HiSqlException("GeneratedKey type can not null!");

        Connection connection = null;
        try {
            connection = getConnection();
            Statement statement = ConnectionUtil.insert(connection, sql, parameters);
            if (statement.getUpdateCount() == 0) return null;
            ResultSet rs = statement.getGeneratedKeys();
            return rs.next() ? rs.getObject(1, keyType) : null;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }


    public int update(
            String sql
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            Statement statement = ConnectionUtil.update(connection, sql, parameters);
            return statement.getUpdateCount();
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }

}
