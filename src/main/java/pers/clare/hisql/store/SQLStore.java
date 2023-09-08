package pers.clare.hisql.store;


import pers.clare.hisql.function.FieldSetter;

import java.lang.reflect.Constructor;
import java.util.Map;

public class SQLStore<T> {
    private final Constructor<T> constructor;
    private final Map<String, FieldSetter> fieldSetMap;

    public SQLStore(Constructor<T> constructor
            , Map<String, FieldSetter> fieldSetMap
    ) {
        this.constructor = constructor;
        this.fieldSetMap = fieldSetMap;
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public Map<String, FieldSetter> getFieldSetMap() {
        return fieldSetMap;
    }
}
