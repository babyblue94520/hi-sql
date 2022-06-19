package pers.clare.hisql.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pers.clare.hisql.data.repository.OptionalRepository;
import pers.clare.hisql.page.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OptionalRepositoryTest {

    private final OptionalRepository optionalRepository;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void find() {
        Object value = 1;
        Optional result = optionalRepository.find(value);
        assertEquals(value, result.orElse(null));
    }

    @Test
    void findAny() {
        Object value = 1;
        Optional<?> result = optionalRepository.findAny(value);
        assertEquals(value, result.orElse(null));
    }

    @Test
    void findGeneric() {
        Object value = 1;
        Optional<Object> result = optionalRepository.findGeneric(value);
        assertEquals(value, result.orElse(null));
    }

    @Test
    void findString() {
        Object value = "TTT";
        Optional<String> result = optionalRepository.findString(value);
        assertEquals(value, result.orElse(null));
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    void findAll() {
        Object value = "TTT";
        Optional<List> result = optionalRepository.findAll(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        assertTrue(result.orElse(Collections.emptyList()).contains(value));
    }

    @Test
    void findAllAny() {
        Object value = "TTT";
        Optional<List<?>> result = optionalRepository.findAllAny(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        assertTrue(result.orElse(Collections.emptyList()).contains(value));
    }

    @Test
    void findAllGeneric() {
        String value = "TTT";
        Optional<List<String>> result = optionalRepository.findAllGeneric(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        assertTrue(result.orElse(Collections.emptyList()).contains(value));

        int value2 = 1;
        Optional<List<Integer>> result2 = optionalRepository.findAllGeneric(value2);
        assertTrue(result2.orElse(Collections.emptyList()).size() > 0);
        assertTrue(result2.orElse(Collections.emptyList()).contains(value2));
    }

    @Test
    void findAllString() {
        String value = "TTT";
        Optional<List<String>> result = optionalRepository.findAllString(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        assertTrue(result.orElse(Collections.emptyList()).contains(value));
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    void findAllMap() {
        String value = "TTT";
        Optional<List<Map>> result = optionalRepository.findAllMap(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        result.orElse(Collections.emptyList()).forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @Test
    void findAllGenericMap() {
        String value = "TTT";
        Optional<List<Map<String, Object>>> result = optionalRepository.findAllGenericMap(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        result.orElse(Collections.emptyList()).forEach(map -> assertEquals(value, map.get("VALUE")));

        int value2 = 1;
        Optional<List<Map<String, Integer>>> result2 = optionalRepository.findAllGenericMap(value2);
        assertTrue(result2.orElse(Collections.emptyList()).size() > 0);
        result2.orElse(Collections.emptyList()).forEach(map -> assertEquals(value2, map.get("VALUE")));
    }

    @Test
    void findAllObjectMap() {
        String value = "TTT";
        Optional<List<Map<String, Object>>> result = optionalRepository.findAllObjectMap(value);
        assertTrue(result.orElse(Collections.emptyList()).size() > 0);
        result.orElse(Collections.emptyList()).forEach(map -> assertEquals(value, map.get("VALUE")));
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    void page() {
        String value = "TTT";
        Optional<Page> result = optionalRepository.page(value);
        Page page = result.orElse(Page.empty());
        assertTrue(page.getRecords().size() > 0);
        assertTrue(page.getRecords().contains(value));

        int value2 = 1;
        Optional<Page> result2 = optionalRepository.page(value2);
        Page page2 = result2.orElse(Page.empty());
        assertTrue(page2.getRecords().size() > 0);
        assertTrue(page2.getRecords().contains(value2));
    }

    @Test
    void pageGeneric() {
        String value = "TTT";
        Optional<Page<String>> result = optionalRepository.pageGeneric(value);
        Page<String> page = result.orElse(Page.empty());
        assertTrue(page.getRecords().size() > 0);
        assertTrue(page.getRecords().contains(value));

        int value2 = 1;
        Optional<Page<Integer>> result2 = optionalRepository.pageGeneric(value2);
        Page<Integer> page2 = result2.orElse(Page.empty());
        assertTrue(page2.getRecords().size() > 0);
        assertTrue(page2.getRecords().contains(value2));
    }
}
