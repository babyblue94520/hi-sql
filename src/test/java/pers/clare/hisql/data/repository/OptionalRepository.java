package pers.clare.hisql.data.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.repository.SQLRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OptionalRepository extends SQLRepository {
    @SuppressWarnings({"rawtypes"})
    @HiSql("select :value")
    Optional find(Object value);

    @HiSql("select :value")
    Optional<?> findAny(Object value);

    @HiSql("select :value")
    <T> Optional<T> findGeneric(T value);

    @HiSql("select :value")
    Optional<String> findString(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("select :value")
    Optional<List> findAll(Object value);

    @HiSql("select :value")
    Optional<List<?>> findAllAny(Object value);

    @HiSql("select :value")
    <T> Optional<List<T>> findAllGeneric(T value);

    @HiSql("select :value")
    Optional<List<String>> findAllString(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("select :value as value")
    Optional<List<Map>> findAllMap(Object value);

    @HiSql("select :value as value")
    <T> Optional<List<Map<String, T>>> findAllGenericMap(T value);

    @HiSql("select :value as value")
    Optional<List<Map<String, Object>>> findAllObjectMap(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("select :value as value")
    Optional<Page> page(Object value);

    @HiSql("select :value as value")
    <T> Optional<Page<T>> pageGeneric(T value);
}
