package pers.clare.hisql.support.field;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public class FieldReflector {
    private final Field field;
    private final Class<?> wrapperType;
    private final FieldGetter getter;
    private final FieldSetter setter;
}
