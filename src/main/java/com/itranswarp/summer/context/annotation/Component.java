package com.itranswarp.summer.context.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * Bean name. Default to simple class name with first-letter-lowercase.
     */
    String value() default "";

}
