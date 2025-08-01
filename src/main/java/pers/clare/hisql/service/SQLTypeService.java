package pers.clare.hisql.service;

import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.util.ResultSetUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SQLTypeService extends SQLBasicService {

    default <T> Map<String, T> findMap(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::toMap);
    }

    default <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::toSet);
    }

    default <T> T find(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::to);
    }

    default <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::toMapSet);
    }

    default <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::toMapList);
    }

    default <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return query(sql, parameters, returnType, ResultSetUtil::toList);
    }

    default <T> Map<String, T> findMap(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::toMap);
    }

    default <T> Set<T> findSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::toSet);
    }

    default <T> T find(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::to);
    }


    default <T> Set<Map<String, T>> findAllMapSet(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::toMapSet);
    }

    default <T> List<Map<String, T>> findAllMap(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::toMapList);
    }

    default <T> List<T> findAll(
            Class<T> returnType
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return query(buildSortSQL(sort, sql), parameters, returnType, ResultSetUtil::toList);
    }

    default <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doPage(clazz, sql, null, parameters);
    }

    default <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPage(clazz, sql, toPagination(sort), parameters);
    }

    default <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(clazz, sql, pagination, parameters);
    }

    private <T> Page<T> doPage(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        pagination = getPagination(pagination);
        if (pagination.getSize() == 0) return Page.empty(pagination);
        String executeSql = buildPaginationSQL(pagination, sql);
        List<T> list = query(executeSql, parameters, clazz, ResultSetUtil::toList);
        return toPage(pagination, list, sql, parameters);
    }


    default <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, null, parameters);
    }

    default <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, toPagination(sort), parameters);
    }

    default <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, pagination, parameters);
    }

    private <T> Page<Map<String, T>> doPageMap(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        pagination = getPagination(pagination);
        if (pagination.getSize() == 0) return Page.empty(pagination);
        String executeSql = buildPaginationSQL(pagination, sql);
        List<Map<String, T>> list = query(executeSql, parameters, clazz, ResultSetUtil::toMapList);
        return toPage(pagination, list, sql, parameters);
    }

}
