package com.chl.excel.annotation;

import com.chl.excel.formatter.DataFormatter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LCH
 * @since 2018-06-13
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     *  generate the column name
     */
    String columnTitle() default "";

    /**
     * default value
     */
    String value() default "";

    /**
     * Marks the location of the generated column,The default value is -1,
     * which means the location of columns generated by read order
     */
    int order() default -1;

    /**
     * Mark whether to generated the columns, support input a expression
     */
    boolean required() default true;


    /**
     *
     */
    Class<? extends DataFormatter> formatter() default DataFormatter.class;
}
