package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SQLQueryService extends SQLBasicService {

    protected String buildSortSQL(Sort sort, String sql) {
        return getPaginationMode().buildSortSQL(sort, sql);
    }

    public <T> Map<String, T> findMap(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::toMap);
    }

    public <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::toSet);
    }

    public <T> T find(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::to);
    }


    public <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::toMapList);
    }

    public <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, null, parameters, ResultSetUtil::toList);
    }

    public <T> Map<String, T> findMap(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::toMap);
    }

    public <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::toSet);
    }

    public <T> T find(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::to);
    }


    public <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::toMapList);
    }

    public <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(returnType, sql, sort, parameters, ResultSetUtil::toList);
    }

    protected <T, R> R queryHandler(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) {
        sql = buildSortSQL(sort, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            return resultSetHandler.apply(getResultSetConverter(), ConnectionUtil.query(connection, sql, parameters), returnType);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }
}
