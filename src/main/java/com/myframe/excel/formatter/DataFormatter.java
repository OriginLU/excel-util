package com.myframe.excel.formatter;

import com.myframe.excel.entity.ExcelColumnConfiguration;

/**
 * @author lch
 * @since 2019-04-28
 */
public interface DataFormatter {


    String format(Object source,Object formatValue);


    Object convertValue(Object formatValue, ExcelColumnConfiguration configuration);
}
