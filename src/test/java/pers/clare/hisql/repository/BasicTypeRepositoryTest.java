package pers.clare.hisql.repository;

import pers.clare.hisql.data.repository.BasicTypeRepository;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BasicTypeRepositoryTest {

    private final BasicTypeRepository basicTypeRepository;

    private final Pagination pagination = Pagination.of(0, 3);

    @Test
    void findInt() {
        int value = 1;
        int result = basicTypeRepository.findInt(value);
        assertEquals(value, result);
    }

    @Test
    void findLong() {
        int value = 1;
        long result = basicTypeRepository.findLong(value);
        assertEquals(value, result);
    }

    @Test
    void findFloat() {
        int value = 1;
        float result = basicTypeRepository.findFloat(value);
        assertEquals(value, result);
    }

    @Test
    void findBoolean() {
        int value = 1;
        boolean result = basicTypeRepository.findBoolean(value);
        assertTrue(result);
    }

    @Test
    void findInteger() {
        int value = 1;
        Integer result = basicTypeRepository.findInteger(value);
        assertEquals(value, result);
    }

    @Test
    void findString() {
        String value = "1";
        String result = basicTypeRepository.findString(value);
        assertEquals(value, result);
    }

    @Test
    void findIntegerMap() {
        int value = 1;
        Map<String, Integer> result = basicTypeRepository.findIntegerMap(value);
        assertEquals(value, result.get("VALUE"));
    }

    @Test
    void findStringMap() {
        String value = "1";
        Map<String, String> result = basicTypeRepository.findStringMap(value);
        assertEquals(value, result.get("VALUE"));
    }

    @Test
    void findIntegerSet() {
        int value = 1;
        Set<Integer> result = basicTypeRepository.findIntegerSet(value);
        assertTrue(result.contains(value));
    }

    @Test
    void findStringSet() {
        String value = "1";
        Set<String> result = basicTypeRepository.findStringSet(value);
        assertTrue(result.contains(value));
    }

    @Test
    void findIntegerMapSet() {
        int value = 1;
        Set<Map<String, Integer>> result = basicTypeRepository.findIntegerMapSet(value);
        result.forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findStringMapSet() {
        String value = "1";
        Set<Map<String, String>> result = basicTypeRepository.findStringMapSet(value);
        result.forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findIntegerList() {
        int value = 1;
        List<Integer> result = basicTypeRepository.findIntegerList(value);
        assertTrue(result.contains(value));
    }

    @Test
    void findStringList() {
        String value = "1";
        List<String> result = basicTypeRepository.findStringList(value);
        assertTrue(result.contains(value));
    }

    @Test
    void findIntegerMapList() {
        int value = 1;
        List<Map<String, Integer>> result = basicTypeRepository.findIntegerMapList(value);
        result.forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findStringMapList() {
        String value = "1";
        List<Map<String, String>> result = basicTypeRepository.findStringMapList(value);
        result.forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findIntegerPage() {
        int value = 1;
        Page<Integer> result = basicTypeRepository.pageInteger(value);
        assertTrue(result.getRecords().contains(value));

        result = basicTypeRepository.pageInteger(pagination, value);
        assertTrue(result.getRecords().contains(value));
    }

    @Test
    void findStringPage() {
        String value = "1";
        Page<String> result = basicTypeRepository.pageString(value);
        assertTrue(result.getRecords().contains(value));

        result = basicTypeRepository.pageString(pagination, value);
        assertTrue(result.getRecords().contains(value));
    }

    @Test
    void findIntegerMapPage() {
        int value = 1;
        Page<Map<String, Integer>> result = basicTypeRepository.pageIntegerMap(value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));

        result = basicTypeRepository.pageIntegerMap(pagination, value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));
    }


    @Test
    void findStringMapPage() {
        String value = "1";
        Page<Map<String, String>> result = basicTypeRepository.pageStringMap(value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));

        result = basicTypeRepository.pageStringMap(pagination, value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findIntegerNext() {
        int value = 1;
        Next<Integer> result = basicTypeRepository.nextInteger(value);
        assertTrue(result.getRecords().contains(value));

        result = basicTypeRepository.nextInteger(pagination, value);
        assertTrue(result.getRecords().contains(value));
    }

    @Test
    void findStringNext() {
        String value = "1";
        Next<String> result = basicTypeRepository.nextString(value);
        assertTrue(result.getRecords().contains(value));

        result = basicTypeRepository.nextString(pagination, value);
        assertTrue(result.getRecords().contains(value));
    }

    @Test
    void findIntegerMapNext() {
        int value = 1;
        Next<Map<String, Integer>> result = basicTypeRepository.nextIntegerMap(value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));

        result = basicTypeRepository.nextIntegerMap(pagination, value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findStringMapNext() {
        String value = "1";
        Next<Map<String, String>> result = basicTypeRepository.nextStringMap(value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));

        result = basicTypeRepository.nextStringMap(pagination, value);
        result.getRecords().forEach(map -> assertEquals(value, map.get("VALUE")));
    }

}
