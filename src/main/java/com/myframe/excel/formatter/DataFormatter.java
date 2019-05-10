package com.myframe.excel.formatter;

/**
 * @author lch
 * @since 2019-04-28
 */
public interface DataFormatter {


    /**
     * format value for export excel file
     */
    String format(Object source,Object formatValue);


    /**
     * convert the available value for object
     */
    Object convertValue(String formatValue);
}
