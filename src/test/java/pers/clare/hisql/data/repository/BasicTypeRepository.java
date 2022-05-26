package pers.clare.hisql.data.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.repository.SQLRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface BasicTypeRepository extends SQLRepository {
    @HiSql("select :value")
    int findInt(int value);

    @HiSql("select :value")
    long findLong(int value);

    @HiSql("select :value")
    float findFloat(int value);

    @HiSql("select :value")
    boolean findBoolean(int value);

    @HiSql("select :value")
    Integer findInteger(int value);

    @HiSql("select :value")
    String findString(String value);

    @HiSql("select :value as value")
    Map<String, Integer> findIntegerMap(int value);

    @HiSql("select :value as value")
    Map<String, String> findStringMap(String value);

    @HiSql("select :value")
    Set<Integer> findIntegerSet(int value);

    @HiSql("select :value")
    Set<String> findStringSet(String value);

    @HiSql("select :value as value")
    Set<Map<String, Integer>> findIntegerMapSet(int value);

    @HiSql("select :value as value")
    Set<Map<String, String>> findStringMapSet(String value);

    @HiSql("select :value")
    List<Integer> findIntegerList(int value);

    @HiSql("select :value")
    List<String> findStringList(String value);

    @HiSql("select :value as value")
    List<Map<String, Integer>> findIntegerMapList(int value);

    @HiSql("select :value as value")
    List<Map<String, String>> findStringMapList(String value);

    @HiSql("select :value")
    Page<Integer> pageInteger(int value);

    @HiSql("select :value")
    Page<Integer> pageInteger(Pagination pagination, int value);

    @HiSql("select :value")
    Page<String> pageString(String value);

    @HiSql("select :value")
    Page<String> pageString(Pagination pagination, String value);

    @HiSql("select :value as value")
    Page<Map<String, Integer>> pageIntegerMap(int value);

    @HiSql("select :value as value")
    Page<Map<String, Integer>> pageIntegerMap(Pagination pagination, int value);

    @HiSql("select :value as value")
    Page<Map<String, String>> pageStringMap(String value);

    @HiSql("select :value as value")
    Page<Map<String, String>> pageStringMap(Pagination pagination, String value);

    @HiSql("select :value")
    Next<Integer> nextInteger(int value);

    @HiSql("select :value")
    Next<Integer> nextInteger(Pagination pagination, int value);

    @HiSql("select :value")
    Next<String> nextString(String value);

    @HiSql("select :value")
    Next<String> nextString(Pagination pagination, String value);

    @HiSql("select :value as value")
    Next<Map<String, Integer>> nextIntegerMap(int value);

    @HiSql("select :value as value")
    Next<Map<String, Integer>> nextIntegerMap(Pagination pagination, int value);

    @HiSql("select :value as value")
    Next<Map<String, String>> nextStringMap(String value);

    @HiSql("select :value as value")
    Next<Map<String, String>> nextStringMap(Pagination pagination, String value);
}
