package net.liyze.basin.mixin;

import com.itranswarp.summer.context.ApplicationContext;
import com.itranswarp.summer.context.BeanDefinition;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import net.liyze.basin.mixin.annotation.Mixin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class MixinProcessor implements Runnable {
    private final List<ApplicationContext> apps;

    public MixinProcessor(List<ApplicationContext> apps) {
        this.apps = apps;
    }

    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    @Override
    public void run() {
        this.apps.forEach(app -> {
            List<BeanDefinition> methods = app.getBeanDefinitions().stream().toList();
            methods.forEach(bean -> Arrays.stream(bean.getBeanClass().getMethods()).filter(method -> method.isAnnotationPresent(Mixin.class))
                    .forEach(mixin -> {
                        try {
                            final ByteBuddy byteBuddy = new ByteBuddy();
                            Mixin annotation = mixin.getAnnotation(Mixin.class);
                            Class<?> target = Class.forName(annotation.target());
                            Method method = target.getMethod(annotation.method(), mixin.getParameterTypes());
                            DynamicType.Unloaded<?> proxy = byteBuddy
                                    .subclass(target, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                                    .method(ElementMatchers.is(method)).intercept(InvocationHandlerAdapter.of(
                                            (proxy1, method1, args) -> mixin.invoke(bean.getInstance(), args)))
                                    .make();
                        } catch (ClassNotFoundException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        });
    }
}
