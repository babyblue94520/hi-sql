package pers.clare.hisql.support;

import pers.clare.hisql.function.ResultSetValueConverter;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultSetConverter {

    private final Map<Class<?>, ResultSetValueConverter<?>> converterMap = new ConcurrentHashMap<>();

    {
        register(InputStream.class, ResultSet::getBinaryStream);
    }

    public void register(Class<?> returnClass, ResultSetValueConverter<?> resultSetValueConverter) {
        converterMap.put(returnClass, resultSetValueConverter);
    }

    @SuppressWarnings("unchecked")
    public <T> ResultSetValueConverter<T> get(Class<T> returnClass) {
        return (ResultSetValueConverter<T>) converterMap.get(returnClass);
    }

}
