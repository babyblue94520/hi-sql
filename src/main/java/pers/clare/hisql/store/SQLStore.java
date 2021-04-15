package pers.clare.hisql.store;


import pers.clare.hisql.function.FieldSetHandler;

import java.lang.reflect.Constructor;
import java.util.*;

public class SQLStore<T> {
    private Constructor<T> constructor;
    private Map<String, FieldSetHandler> fieldSetMap;

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
