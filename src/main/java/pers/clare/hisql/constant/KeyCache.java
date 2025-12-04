package pers.clare.hisql.constant;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class KeyCache {
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public static String computeIfAbsent(String key) {
        return cache.computeIfAbsent(key, k -> k);
    }

    public static String get(String key) {
        return cache.get(key);
    }
}
