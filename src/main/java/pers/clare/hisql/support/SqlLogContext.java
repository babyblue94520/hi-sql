package pers.clare.hisql.support;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class SqlLogContext {

    private static final ThreadLocal<Class<?>> currentCaller = new ThreadLocal<>();

    public static void setCaller(Class<?> caller) {
        currentCaller.set(caller);
    }

    public static void debug(String sql) {
        Logger log = LogManager.getLogger(currentCaller.get().getName());
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
    }
}
