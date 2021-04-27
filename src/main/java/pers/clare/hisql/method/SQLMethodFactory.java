package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.page.*;
import pers.clare.hisql.util.MethodUtil;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.exception.HiSqlException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;


public class SQLMethodFactory {
    // check string is select sql
    private static final Pattern select = Pattern.compile("^[\\s\\n]?s", Pattern.CASE_INSENSITIVE);

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            HiSqlContext context
            , SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        Method[] methods = repositoryInterface.getDeclaredMethods();
        Map<String, String> contents = SQLInjector.getContents(context.getXmlRoot(), repositoryInterface);
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        String command;
        SQLMethod sqlMethod;
        for (Method method : methods) {
            command = findSqlCommand(contents, method);
            if (command == null) {
                throw new HiSqlException("%s.%s method must set XML or Sql.query", repositoryInterface.getName(), method.getName());
            }
            sqlMethod = buildMethod(method, command);
            if (sqlMethod == null) {
                throw new HiSqlException("%s not support return type", method.getName());
            }
            sqlMethod.setContext(context);
            sqlMethod.setMethod(method);
            sqlMethod.setSql(command);
            sqlMethod.setSqlStoreService(sqlStoreService);
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
        Class<?> valueType = MethodUtil.getReturnClass(method, 0);
        if (valueType == null || valueType == Map.class) {
            return new BasicTypeMapSet(getParameterMapValueClass(method, valueType));
        } else {
            if (SQLStoreFactory.isIgnore(valueType)) {
                return new BasicTypeSet(valueType);
            } else {
                return new SQLEntitySet(valueType);
            }
        }
    }

    /**
     * build list result method interceptor
     */
    private static SQLMethod buildList(Method method) {
        Class<?> valueType = MethodUtil.getReturnClass(method, 0);
        if (valueType == null || valueType == Map.class) {
            return new BasicTypeMapList(getParameterMapValueClass(method, valueType));
        } else {
            if (SQLStoreFactory.isIgnore(valueType)) {
                return new BasicTypeList(valueType);
            } else {
                return new SQLEntityList(valueType);
            }
        }
    }

    /**
     * build page result method interceptor
     */
    private static SQLMethod buildPage(Method method) {
        Class<?> valueType = MethodUtil.getReturnClass(method, 0);
        if (valueType == null || valueType == Map.class) {
            valueType = getParameterMapValueClass(method, null);
            return new BasicTypeMapPage(valueType);
        } else {
            if (SQLStoreFactory.isIgnore(valueType)) {
                return new BasicTypPage(valueType);
            } else {
                return new SQLEntityPage(valueType);
            }
        }
    }

    /**
     * build next result method interceptor
     */
    private static SQLMethod buildNext(Method method) {
        Class<?> valueType = MethodUtil.getReturnClass(method, 0);
        if (valueType == null || valueType == Map.class) {
            valueType = getParameterMapValueClass(method, null);
            return new BasicTypeMapNext(valueType);
        } else {
            if (SQLStoreFactory.isIgnore(valueType)) {
                return new BasicTypeNext(valueType);
            } else {
                return new SQLEntityNext(valueType);
            }
        }
    }

    private static int sortIndexOf(Class<?>[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++)
            if (parameterTypes[i] == Sort.class) return i;
        return -1;
    }

    private static int paginationIndexOf(Class<?>[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++)
            if (parameterTypes[i] == Pagination.class) return i;
        return -1;
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
        Class<?> valueType = MethodUtil.getReturnClass(method, 0);
        return valueType == null ? Object.class : valueType;
    }

    private static Class<?> getParameterMapValueClass(Method method, Class<?> valueType) {
        if (valueType == null) return Object.class;
        valueType = (Class<?>) MethodUtil.getType(MethodUtil.getReturnType(method, 0), 1);
        return valueType == null ? Object.class : valueType;
    }
}
