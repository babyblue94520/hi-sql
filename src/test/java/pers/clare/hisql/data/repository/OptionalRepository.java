package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.repository.SQLRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OptionalRepository extends SQLRepository {
    @SuppressWarnings({"rawtypes"})
    @HiSql("SELECT :value")
    Optional find(Object value);

    @HiSql("SELECT :value")
    Optional<?> findAny(Object value);

    @HiSql("SELECT :value")
    <T> Optional<T> findGeneric(T value);

    @HiSql("SELECT :value")
    Optional<String> findString(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("SELECT :value")
    Optional<List> findAll(Object value);

    @HiSql("SELECT :value")
    Optional<List<?>> findAllAny(Object value);

    @HiSql("SELECT :value")
    <T> Optional<List<T>> findAllGeneric(T value);

    @HiSql("SELECT :value")
    Optional<List<String>> findAllString(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("SELECT :value AS value")
    Optional<List<Map>> findAllMap(Object value);

    @HiSql("SELECT :value AS value")
    <T> Optional<List<Map<String, T>>> findAllGenericMap(T value);

    @HiSql("SELECT :value AS value")
    Optional<List<Map<String, Object>>> findAllObjectMap(Object value);

    @SuppressWarnings({"rawtypes"})
    @HiSql("SELECT :value AS value")
    Optional<Page> page(Object value);

    @HiSql("SELECT :value AS value")
    <T> Optional<Page<T>> pageGeneric(T value);
}
