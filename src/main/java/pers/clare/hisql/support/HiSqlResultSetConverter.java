package pers.clare.hisql.support;

import pers.clare.hisql.function.ResultSetValueConverter;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiSqlResultSetConverter {

    private static final Map<Class<?>, ResultSetValueConverter<?>> converter = new ConcurrentHashMap<>();

    static {
        register(InputStream.class, ResultSet::getBinaryStream);
    }

    private HiSqlResultSetConverter() {
    }

    public static void register(Class<?> clazz, ResultSetValueConverter<?> resultSetValueConverter) {
        converter.put(clazz, resultSetValueConverter);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResultSetValueConverter<T> get(Class<T> clazz) {
        return (ResultSetValueConverter<T>) converter.get(clazz);
    }

}
