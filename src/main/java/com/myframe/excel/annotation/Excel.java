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
     * specified name for the title of excel file and output file
     */
    String value() default "";

    /**
     * the specified version is used to generate matching excel file,default create
     * 2003's excel file,about version please see {@link VersionConstant}
     */
    String version() default VersionConstant.EXCEL_2003;

    /**
     * not create title for excel file if false
     */
    boolean isCreateTitle() default true;
}
