package pers.clare.hisql.util;


import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@UtilityClass
public class ExceptionUtil {

    public static <T extends Throwable> T insertBefore(Method method, T throwable) {
        return insertBefore(method, throwable, className -> className.contains(".hisql."));
    }

    public static <T extends Throwable> T insertBefore(Method method, T throwable, Predicate<String> insertPackageCondition) {
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        List<StackTraceElement> newStackTraces = new ArrayList<>(stackTraces.length + 1);
        for (StackTraceElement stackTrace : stackTraces) {
            newStackTraces.add(stackTrace);
            if (stackTrace.getClassName().contains("$Proxy")) {
                newStackTraces.add(build(method));
            }
        }
        throwable.setStackTrace(newStackTraces.toArray(new StackTraceElement[0]));
        return throwable;
    }

    public static StackTraceElement build(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return new StackTraceElement(
                clazz.getName()
                , method.getName()
                , String.format("%s.java", clazz.getSimpleName())
                , 0
        );
    }
}
