package net.liyze.basin.summer.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Around {

    /**
     * Invocation handler bean name.
     */
    String value();

}
