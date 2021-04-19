package pers.clare.hisql.repository;

import pers.clare.hisql.page.*;

import java.util.List;

public interface SQLCrudRepository<T> extends SQLRepository {

    long count();

    long count(T entity);

    long countById(Object... keys);

    List<T> findAll();

    List<T> findAll(Sort sort);

    Page<T> page(Pagination pagination);

    Next<T> next(Pagination pagination);

    T find(T entity);

    T findById(Object... keys);

    T insert(T entity);

    int update(T entity);

    int delete(T entity);

    int deleteById(Object... keys);

    int deleteAll();
}
