package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.support.SqlLogContext;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.ExceptionUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SQLMethodInterceptor implements MethodInterceptor {
    private final Class<?> interfaceClass;
    private final SQLService service;
    private final Object target;
    private final Map<Method, Method> methodMap = new HashMap<>();
    private final Map<Method, MethodInterceptor> methodInterceptorMap;

    public SQLMethodInterceptor(
            Class<?> interfaceClass
            , SQLService service
            , Object target
    ) {
        this.interfaceClass = interfaceClass;
        this.service = service;
        this.target = target;
        this.methodInterceptorMap = SQLMethodFactory.create(interfaceClass, service);
        Class<?> targetClass = target.getClass();
        Method targetMethod;
        for (Method method : ClassUtil.getMethods(interfaceClass)) {
            try {
                targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                this.methodMap.put(method, targetMethod);
            } catch (NoSuchMethodException ignored) {
            }
        }
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            Method method = this.methodMap.get(methodInvocation.getMethod());
            SqlLogContext.setCaller(interfaceClass);
            if (method == null) {
                MethodInterceptor handler = methodInterceptorMap.get(methodInvocation.getMethod());
                if (handler == null)
                    throw new HiSqlException("%s not found", methodInvocation.getMethod());
                return handler.invoke(methodInvocation);
            } else {
                return method.invoke(target, methodInvocation.getArguments());
            }
        } catch (Throwable e) {
            throw ExceptionUtil.insertBefore(methodInvocation.getMethod(), e);
        }
    }
}
