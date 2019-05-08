package com.myframe.excel.annotation;

import com.myframe.excel.constant.VersionConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LCH
 * @since 2018-06-13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    String value() default "";

    String version() default VersionConstant.EXCEL_2007_ADV;

    boolean isCreateTitle() default true;
}
