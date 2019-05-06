package com.myframe.jdbc.extension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author lch
 * @since 2019-03-16
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Select {


    String baseSQL() default "";

    String refCondition() default "";


}
