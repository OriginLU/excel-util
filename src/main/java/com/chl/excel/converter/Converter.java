package com.chl.excel.converter;

/**
 * @author lch
 * @since 2018-08-20
 */
public interface Converter<T>{


    String toString(Object obj,String pattern);


    T convertTo(Object obj,String pattern);
}
