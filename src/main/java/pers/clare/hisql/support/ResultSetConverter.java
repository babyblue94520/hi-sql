package pers.clare.hisql.support;

import pers.clare.hisql.function.ResultSetConvertHandler;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultSetConverter {

    private final Map<Class<?>, ResultSetConvertHandler<?>> map = new ConcurrentHashMap<>();

    {
        register(InputStream.class, ResultSet::getBinaryStream);
    }

    public <T> void register(Class<T> type, ResultSetConvertHandler<T> converter) {
        map.put(type, converter);
    }

    public <T> ResultSetConvertHandler<T> get(Class<T> type) {
        return (ResultSetConvertHandler<T>) map.get(type);
    }

}
