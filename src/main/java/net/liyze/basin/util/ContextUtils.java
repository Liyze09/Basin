package net.liyze.basin.util;

import com.itranswarp.summer.ApplicationContext;
import com.itranswarp.summer.BeanDefinition;
import net.liyze.basin.core.Basin;
import org.jetbrains.annotations.Nullable;

public class ContextUtils {
    public static @Nullable BeanDefinition getBean(String name) {
        for (ApplicationContext context : Basin.contexts) {
            BeanDefinition bean = context.findBeanDefinition(name);
            if (bean != null) return bean;
        }
        return null;
    }

}
