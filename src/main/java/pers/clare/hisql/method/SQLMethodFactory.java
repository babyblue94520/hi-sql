package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.MethodUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class SQLMethodFactory {
    // check string is select sql
    private static final Pattern select = Pattern.compile("^[\\s\\n]?s", Pattern.CASE_INSENSITIVE);

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        HiSqlContext context = sqlStoreService.getContext();
        Method[] methods = repositoryInterface.getDeclaredMethods();
        Map<String, String> contents = SQLInjector.getContents(context.getXmlRoot(), repositoryInterface);
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        String command;
        SQLMethod sqlMethod;
        boolean readonly;
        HiSql hiSql;
        for (Method method : methods) {
            hiSql = method.getAnnotation(HiSql.class);
            readonly = hiSql != null && hiSql.readonly();
            command = contents.get(method.getName());
            if (command == null && hiSql != null) {
                command = contents.get(hiSql.name());
                if (command == null) command = hiSql.value();
            }
            if (command == null || command.length() == 0) {
                throw new HiSqlException("%s.%s method must set XML or Sql.query", repositoryInterface.getName(), method.getName());
            }
            sqlMethod = buildMethod(method, command);
            if (sqlMethod == null) {
                throw new HiSqlException("%s not support return type", method.getName());
            }
            sqlMethod.setSqlStoreService(sqlStoreService);
            sqlMethod.setMethod(method);
            sqlMethod.setSql(command);
            sqlMethod.setReadonly(readonly);
            sqlMethod.init();
            methodInterceptors.put(method, sqlMethod);
        }
        return methodInterceptors;
    }

    /**
     * build method interceptor by command type
     */
    private static SQLMethod buildMethod(Method method, String command) {
        Class<?> returnType = method.getReturnType();
        if (select.matcher(command).find()) {
            if (Collection.class.isAssignableFrom(returnType)) {
                if (returnType == Set.class) {
                    return buildSet(method);
                } else {
                    return buildList(method);
                }
            } else if (returnType == Map.class) {
                return new BasicTypeMap(getMapValueClass(method));
            } else if (Page.class.isAssignableFrom(returnType)) {
                return buildPage(method);
            } else if (Next.class.isAssignableFrom(returnType)) {
                return buildNext(method);
            } else {
                if (SQLStoreFactory.isIgnore(returnType)) {
                    return new BasicType(returnType);
                } else {
                    return new SQLEntity(returnType);
                }
            }
        } else {
            if (returnType == void.class || returnType == Integer.class || returnType == int.class) {
                return new SQLUpdateMethod();
            } else if (returnType == Long.class || returnType == long.class) {
                return new SQLUpdateLongMethod();
            } else {
                throw new HiSqlException("%s return type must be int or long", method);
            }
        }
    }

    /**
     * build set result method interceptor
     */
    private static SQLMethod buildSet(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            return new BasicTypeMapSet(getParameterMapValueClass(method, returnType));
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return new BasicTypeSet(returnType);
            } else {
                return new SQLEntitySet(returnType);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SQLMethod buildList(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            return new BasicTypeMapList(getParameterMapValueClass(method, returnType));
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return new BasicTypeList(returnType);
            } else {
                return new SQLEntityList(returnType);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SQLMethod buildPage(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            returnType = getParameterMapValueClass(method, null);
            return new BasicTypeMapPage(returnType);
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return new BasicTypPage(returnType);
            } else {
                return new SQLEntityPage(returnType);
            }
        }
    }

    /**
     * build next result method interceptor
     */
    private static SQLMethod buildNext(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        if (returnType == null || returnType == Map.class) {
            returnType = getParameterMapValueClass(method, null);
            return new BasicTypeMapNext(returnType);
        } else {
            if (SQLStoreFactory.isIgnore(returnType)) {
                return new BasicTypeNext(returnType);
            } else {
                return new SQLEntityNext(returnType);
            }
        }
    }

    /**
     * get sql string
     */
    private static String findSqlCommand(
            Map<String, String> contents
            , Method method
    ) {
        String command = contents.get(method.getName());
        if (command == null) {
            HiSql hiSql = method.getAnnotation(HiSql.class);
            if (hiSql != null) {
                command = contents.get(hiSql.name());
                if (command == null) command = hiSql.value();
            }
        }
        return command;
    }

    private static Class<?> getMapValueClass(Method method) {
        Class<?> returnType = MethodUtil.getReturnClass(method, 0);
        return returnType == null ? Object.class : returnType;
    }

    private static Class<?> getParameterMapValueClass(Method method, Class<?> returnType) {
        if (returnType == null) return Object.class;
        returnType = (Class<?>) MethodUtil.getType(MethodUtil.getReturnType(method, 0), 1);
        return returnType == null ? Object.class : returnType;
    }
}
