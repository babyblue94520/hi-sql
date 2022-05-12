package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.constant.CommandType;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.MethodUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SQLMethodFactory {

    private SQLMethodFactory() {
    }

    public static Map<Method, MethodInterceptor> create(
            SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        Map<Method, MethodInterceptor> methodInterceptors = new HashMap<>();
        buildMethod(methodInterceptors, sqlStoreService, repositoryInterface);
        return methodInterceptors;
    }

    private static void buildMethod(
            Map<Method, MethodInterceptor> methodInterceptors
            , SQLStoreService sqlStoreService
            , Class<?> repositoryInterface
    ) {
        if (repositoryInterface == null
                || repositoryInterface == SQLRepository.class
                || repositoryInterface == SQLCrudRepository.class
        ) return;
        Class<?>[] superInterfaces = repositoryInterface.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            buildMethod(methodInterceptors, sqlStoreService, superInterface);
        }
        HiSqlContext context = sqlStoreService.getContext();
        Method[] methods = repositoryInterface.getDeclaredMethods();
        Map<String, String> contents = SQLInjector.getContents(context.getXmlRoot(), repositoryInterface);
        String command;
        SQLMethod sqlMethod;
        boolean readonly;
        HiSql hiSql;
        for (Method method : methods) {
            if (methodInterceptors.containsKey(method)) continue;
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
            int commandType = SQLQueryUtil.getCommandType(command);
            sqlMethod = buildMethod(method, commandType);
            if (sqlMethod == null) {
                throw new HiSqlException("%s not support return type", method.getName());
            }
            sqlMethod.setSqlStoreService(sqlStoreService);
            sqlMethod.setMethod(method);
            sqlMethod.setCommandType(commandType);
            sqlMethod.setSql(command);
            sqlMethod.setReadonly(readonly);
            sqlMethod.init();
            methodInterceptors.put(method, sqlMethod);
        }
    }

    /**
     * build method interceptor by command type
     */
    private static SQLMethod buildMethod(Method method, int commandType) {
        Class<?> returnType = method.getReturnType();
        switch (commandType) {
            case CommandType.Select:
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
            case CommandType.Insert:
                return new SQLInsertMethod(returnType);
            default:
                return new SQLUpdateMethod(returnType);
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
