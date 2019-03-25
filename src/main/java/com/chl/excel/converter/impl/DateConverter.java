package com.chl.excel.converter.impl;

import com.chl.excel.converter.Converter;
import com.chl.excel.exception.DateParseErrorException;
import com.chl.excel.exception.TypeErrorException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lch
 * @since 2018-08-20
 */
public class DateConverter implements Converter<Date> {


    @Override
    public String toString(Object obj, String pattern) {

        if (obj instanceof java.util.Date && obj instanceof Long) {
            throw new TypeErrorException("input type[" + obj.getClass() + "] error,"
                    + ",support type only java.util.Date and java.lang.Long,check please");
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(obj);
    }


    @Override
    public Date convertTo(Object obj, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse((String) obj);
        }catch (Exception e){
            throw new DateParseErrorException("date parse error ",e);
        }
    }
}
