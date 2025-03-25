package pers.clare.hisql.store;


import lombok.Getter;
import pers.clare.hisql.function.FieldSetter;

import java.lang.reflect.Constructor;
import java.util.Map;

@Getter
public class SQLStore<T> {
    protected final Constructor<T> constructor;
    protected final Map<String, FieldSetter> fieldSetterMap;

    public SQLStore(Constructor<T> constructor
            , Map<String, FieldSetter> fieldSetterMap
    ) {
        this.constructor = constructor;
        this.fieldSetterMap = fieldSetterMap;
    }

}
