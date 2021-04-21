package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.clare.hisql.exception.HiSqlException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SQLMethodInterceptor implements MethodInterceptor {
    private static final Logger log = LogManager.getLogger();

    private final Object target;
    private final Map<Method, Method> methods = new HashMap<>();
    private final Map<Method, MethodInterceptor> queryMethods;

    public SQLMethodInterceptor(
            Class<?> interfaceClass
            , Map<Method, MethodInterceptor> queryMethods
            , Object target
    ) {
        this.queryMethods = queryMethods;
        this.target = target;
        Class<?> targetClass = target.getClass();
        Method targetMethod;
        for (Method method : interfaceClass.getMethods()) {
            try {
                targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                this.methods.put(method, targetMethod);
            } catch (NoSuchMethodException e) {
//                log.warn(e.getMessage());
            }
        }
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = this.methods.get(methodInvocation.getMethod());
        if (method == null) {
            MethodInterceptor handler = queryMethods.get(methodInvocation.getMethod());
            if (handler == null)
                throw new HiSqlException("%s not found", methodInvocation.getMethod());
            return handler.invoke(methodInvocation);
        } else {
            return method.invoke(target, methodInvocation.getArguments());
        }
    }
}
