package pers.clare.hisql.store;


import pers.clare.hisql.function.FieldSetHandler;

import java.lang.reflect.Constructor;
import java.util.Map;

public class SQLStore<T> {
    private final Constructor<T> constructor;
    private final Map<String, FieldSetHandler> fieldSetMap;

    public SQLStore(Constructor<T> constructor
            , Map<String, FieldSetHandler> fieldSetMap
    ) {
        this.constructor = constructor;
        this.fieldSetMap = fieldSetMap;
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public Map<String, FieldSetHandler> getFieldSetMap() {
        return fieldSetMap;
    }
}
