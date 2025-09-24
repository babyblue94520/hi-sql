package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.util.ExceptionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SQLMethodInterceptor implements MethodInterceptor {
    private final Object target;
    private final Map<Method, Method> methodMap = new HashMap<>();
    private final Map<Method, MethodInterceptor> methodInterceptorMap;

    public SQLMethodInterceptor(
            Class<?> interfaceClass
            , Object target
            , Map<Method, MethodInterceptor> methodInterceptorMap
    ) {
        this.methodInterceptorMap = methodInterceptorMap;
        this.target = target;
        Class<?> targetClass = target.getClass();
        Method targetMethod;
        for (Method method : interfaceClass.getMethods()) {
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
            if (method == null) {
                MethodInterceptor handler = methodInterceptorMap.get(methodInvocation.getMethod());
                if (handler == null)
                    throw new HiSqlException("%s not found", methodInvocation.getMethod());
                return handler.invoke(methodInvocation);
            } else {
                try {
                    return method.invoke(target, methodInvocation.getArguments());
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        } catch (Throwable e) {
            throw ExceptionUtil.insertBefore(methodInvocation.getMethod(), e);
        }
    }
}
