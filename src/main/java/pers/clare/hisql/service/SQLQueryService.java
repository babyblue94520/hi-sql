package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class SQLQueryService extends SQLBasicService {

    public SQLQueryService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    protected String buildSortSQL(Sort sort, String sql) {
        return context.getPaginationMode().buildSortSQL(sort, sql);
    }

    public <T> Map<String, T> find(
            String sql
            , Class<T> returnType
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::toMap);
    }

    public <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::toSet);
    }

    public <T> T findFirst(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::to);
    }


    public <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::toMapList);
    }

    public <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sql, null, returnType, parameters, ResultSetUtil::toList);
    }

    public <T> Map<String, T> find(
            String sql
            , Sort sort
            , Class<T> returnType
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::toMap);
    }

    public <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::toSet);
    }

    public <T> T findFirst(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::to);
    }


    public <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::toMapList);
    }

    public <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sql, sort, returnType, parameters, ResultSetUtil::toList);
    }

    protected <T, R> R queryHandler(
            String sql
            , Sort sort
            , Class<T> returnType
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) {
        sql = buildSortSQL(sort, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            return resultSetHandler.apply(ConnectionUtil.query(connection, sql, parameters), returnType);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }
}
