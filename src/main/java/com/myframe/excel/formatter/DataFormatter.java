package com.myframe.excel.formatter;

/**
 * @author lch
 * @since 2019-04-28
 */
public interface DataFormatter {


    String format(Object source,Object formatValue);


    Object convertValue(String formatValue);
}
