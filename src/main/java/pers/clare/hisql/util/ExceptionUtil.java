package pers.clare.hisql.util;


import java.lang.reflect.Method;
import java.util.function.Function;

public class ExceptionUtil {

    public static <T extends Throwable> T insertBefore(Method method, T throwable) {
        return insertBefore(method, throwable, (className) -> className.contains(".hisql."));
    }

    public static <T extends Throwable> T insertBefore(Method method, T throwable, Function<String, Boolean> insertPackageCondition) {
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        StackTraceElement[] newStackTraces = new StackTraceElement[stackTraces.length + 1];
        int i = 0;
        for (; i < stackTraces.length; i++) {
            StackTraceElement stackTrace = (newStackTraces[i] = stackTraces[i]);
            if (insertPackageCondition.apply(stackTrace.getClassName())) break;
        }
        for (; i < stackTraces.length; i++) {
            StackTraceElement stackTrace = (newStackTraces[i] = stackTraces[i]);
            if (!insertPackageCondition.apply(stackTrace.getClassName())) break;
        }
        newStackTraces[i] = build(method);
        System.arraycopy(stackTraces, i, newStackTraces, i + 1, stackTraces.length - i);
        throwable.setStackTrace(newStackTraces);
        return throwable;
    }

    public static <T extends Throwable> T insertAfter(Method method, T throwable) {
        return insertAfter(method, throwable, (className) -> className.contains(".hisql."));
    }

    public static <T extends Throwable> T insertAfter(Method method, T throwable, Function<String, Boolean> insertPackageCondition) {
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        StackTraceElement[] newStackTraces = new StackTraceElement[stackTraces.length + 1];
        int i = 0;
        for (; i < stackTraces.length; i++) {
            StackTraceElement stackTrace = stackTraces[i];
            if (insertPackageCondition.apply(stackTrace.getClassName())) {
                newStackTraces[i] = build(method);
                break;
            }
            newStackTraces[i] = stackTraces[i];
        }
        System.arraycopy(stackTraces, i, newStackTraces, i + 1, stackTraces.length - i);
        throwable.setStackTrace(newStackTraces);
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
