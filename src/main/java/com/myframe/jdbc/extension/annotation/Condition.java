package com.myframe.jdbc.extension.annotation;

import com.myframe.jdbc.extension.expression.ExpressionConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * @author lch
 * @since 2019-03-16
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Condition {


    /**
     * must be unique
     */
    String name() default "";

    String expression() default ExpressionConstants.TRUE;

    String condition();
}
