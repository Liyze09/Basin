package net.liyze.basin.summer.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;

/**
 * Create proxy by subclassing and override methods with interceptor.
 */
public class ProxyResolver {

    private static ProxyResolver INSTANCE = null;
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ByteBuddy byteBuddy = new ByteBuddy();

    private ProxyResolver() {
    }

    public static ProxyResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProxyResolver();
        }
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T bean, InvocationHandler handler) {
        Class<?> targetClass = bean.getClass();
        logger.atDebug().log("create proxy for bean {} @{}", targetClass.getName(), Integer.toHexString(bean.hashCode()));
        Class<?> proxyClass = this.byteBuddy
                // subclass with default empty constructor:
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // intercept methods:
                .method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                        // proxy method invoke:
                        (proxy, method, args) -> {
                            // delegate to origin bean:
                            return handler.invoke(bean, method, args);
                        }))
                // generate proxy class:
                .make().load(targetClass.getClassLoader()).getLoaded();
        Object proxy;
        try {
            proxy = proxyClass.getConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }
}
