package pers.clare.hisql.repository;

import org.springframework.lang.NonNull;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.util.Collection;
import java.util.List;

public interface SQLCrudRepository<E, K> extends SQLRepository {

    @NonNull
    long count();

    @NonNull
    long count(E entity);

    @NonNull
    List<E> findAll();

    @NonNull
    List<E> findAll(Sort sort);

    @NonNull
    Page<E> page(Pagination pagination);

    @NonNull
    E insert(@NonNull E entity);

    @NonNull
    int update(E entity);

    @NonNull
    int delete(E entity);

    @NonNull
    Collection<E> insertAll(@NonNull Collection<E> entities);

    @NonNull
    E[] insertAll(@NonNull E[] entities);

    @NonNull
    int[] updateAll(@NonNull Collection<E> entities);

    @NonNull
    int[] updateAll(@NonNull E[] entities);

    @NonNull
    int deleteAll();

    @NonNull
    int[] deleteAll(@NonNull Collection<E> entities);

    @NonNull
    int[] deleteAll(@NonNull E[] entities);

    @NonNull
    long countById(K key);

    @NonNull
    int deleteById(K key);

    int deleteByIds(K[] keys);

    E find(E entity);

    E findById(K key);

    List<E> findAllByIds(K[] key);

    <T> T findByObject(T object);

    <T> T insertByObject(T object);

    <T> int updateByObject(T object);

    <T> int deleteByObject(T object);
}
