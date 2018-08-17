package com.chl.excel.util;

import com.chl.excel.exception.DateParseErrorException;
import com.chl.excel.exception.TypeErrorException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lch
 * @since 2018-08-16
 */
public abstract class DateFormatUtils {


    public static String toString(Object obj, String pattern){

        if (obj instanceof Date && obj instanceof Long){
            throw new TypeErrorException("input type[" + obj.getClass() + "] error,"
                    + ",support type only java.util.Date and java.lang.Long,check please");
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(obj);
    }

    public static Date toDate(String src,String pattern){

        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse(src);
        }catch (Exception e){
            throw new DateParseErrorException("date parse error ",e);
        }
    }
}
