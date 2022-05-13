package pers.clare.hisql.util;

public class ClassUtil {

    public static boolean isBasicType(Class<?> type) {
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

}
