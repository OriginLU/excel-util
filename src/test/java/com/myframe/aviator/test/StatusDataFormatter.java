package com.myframe.aviator.test;

import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.formatter.DataFormatter;

/**
 * @author lch
 * @since 2019-05-01
 */
public class StatusDataFormatter implements DataFormatter {


    @Override
    public String format(Object source, Object formatValue) {

        String status = (String) formatValue;

        if ("1".equals(status))
        {
            return "成功";
        }
        return "失败";
    }

    @Override
    public Object convertValue(Object formatValue, ExcelColumnConfiguration configuration) {


        String status = (String) formatValue;

        if ("成功".equals(status))
        {
            return "1";
        }
        return "0";
    }
}
