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

    /**
     * create excel name
     */
    String value() default "";

    /**
     * specified version to generate excel file formats
     */
    String version() default VersionConstant.EXCEL_2003;

    /**
     * will be not create title in file if false
     */
    boolean isCreateTitle() default true;
}
