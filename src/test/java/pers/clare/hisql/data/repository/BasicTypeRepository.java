package pers.clare.hisql.data.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.repository.SQLRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface BasicTypeRepository extends SQLRepository {
    @HiSql("SELECT ?")
    int findInt(int value);

    @HiSql("SELECT :value")
    long findLong(int value);

    @HiSql("SELECT :value")
    float findFloat(int value);

    @HiSql("SELECT :value")
    boolean findBoolean(int value);

    @HiSql("SELECT :value WHERE false")
    int notFoundInt(int value);

    @HiSql("SELECT :value WHERE false")
    long notFoundLong(int value);

    @HiSql("SELECT :value WHERE false")
    float notFoundFloat(int value);

    @HiSql("SELECT :value WHERE false")
    boolean notFoundBoolean(int value);

    @HiSql("SELECT :value")
    Integer findInteger(int value);

    @HiSql("SELECT :value")
    String findString(String value);

    @HiSql("SELECT :value AS value")
    Map<String, Integer> findIntegerMap(int value);

    @HiSql("SELECT :value AS value")
    Map<String, String> findStringMap(String value);

    @HiSql("SELECT ? AS value, ? AS value2")
    Map<String, String> findStringMap2(String value, String value2);

    @HiSql("SELECT :value")
    Set<Integer> findIntegerSet(int value);

    @HiSql("SELECT :value")
    Set<String> findStringSet(String value);

    @HiSql("SELECT :value AS value")
    Set<Map<String, Integer>> findIntegerMapSet(int value);

    @HiSql("SELECT :value AS value")
    Set<Map<String, String>> findStringMapSet(String value);

    @HiSql("SELECT :value")
    List<Integer> findIntegerList(int value);

    @HiSql("SELECT :value")
    List<String> findStringList(String value);

    @HiSql("SELECT :value AS value")
    List<Map<String, Integer>> findIntegerMapList(int value);

    @HiSql("SELECT ? AS value")
    List<Map<String, String>> findStringMapList(String value);

    @HiSql("SELECT :value")
    Page<Integer> pageInteger(int value);

    @HiSql("SELECT :value")
    Page<Integer> pageInteger(Pagination pagination, int value);

    @HiSql("SELECT :value")
    Page<String> pageString(String value);

    @HiSql("SELECT :value")
    Page<String> pageString(Pagination pagination, String value);

    @HiSql("SELECT :value AS value")
    Page<Map<String, Integer>> pageIntegerMap(int value);

    @HiSql("SELECT :value AS value")
    Page<Map<String, Integer>> pageIntegerMap(Pagination pagination, int value);

    @HiSql("SELECT :value AS value")
    Page<Map<String, String>> pageStringMap(String value);

    @HiSql("SELECT :value AS value")
    Page<Map<String, String>> pageStringMap(Pagination pagination, String value);
}
