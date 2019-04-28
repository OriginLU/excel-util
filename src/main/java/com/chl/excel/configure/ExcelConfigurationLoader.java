package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.ExcelCreateException;
import com.chl.excel.exception.RepeatOrderException;
import com.chl.excel.formatter.DataFormatter;
import com.chl.excel.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 获取excel配置信息
 *
 * @author LCH
 * @since 2018-07-02
 */
public abstract class ExcelConfigurationLoader {


    public static String getExcelTitleName(Class<?> type) {

        Excel annotation = type.getAnnotation(Excel.class);
        return annotation.value();
    }

    public static String getExcelVersion(Class<?> type) {

        Excel annotation = type.getAnnotation(Excel.class);
        return annotation.version();
    }

    public static ExcelColumnConf[] getExcelColumnConfiguration(List<Member> members){


        int length = members.size();
        Set<Integer> orders = new HashSet<>();
        ExcelColumnConf[] conf = new ExcelColumnConf[length];
        LinkedList<Integer> index = new LinkedList<>();

        for (int col = 0; col < length; col++)
        {
            Member member = members.get(col);
            ExcelColumnConf columnConf = createExcelColumnConf(member);
            ExcelColumn excelColumn = ReflectUtils.getMemberAnnotation(member, ExcelColumn.class);
            Integer order = excelColumn.order();

            if (order > -1)
            {
                if (!orders.add(order))
                {
                    String memberName = member.getDeclaringClass().getName();
                    throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                            " in the member [" + memberName + "." + member.getName() + "]," + "which same as the" +
                            " member [" + memberName + "." + conf[order].getMember().getName() + "]");
                }
            }
            else
            {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
            }

            if (conf[order] != null)
            {
                Integer tempIndex = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
                ExcelColumnConf temp = conf[order];
                conf[order] = columnConf;
                conf[tempIndex] = temp;
            }
            else
            {
                conf[order] = columnConf;
            }

            if (!index.contains(order = getFreeIndex(col, conf)) && col != length - 1)
            {
                index.add(order);
            }
        }
        return conf;
    }


    private static ExcelColumnConf createExcelColumnConf(Member member) {

        ExcelColumnConf column = new ExcelColumnConf();
        Annotation[] annotations = ReflectUtils.getMemberAnnotations(member);
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType() == ExcelColumn.class)
            {
                ExcelColumn col = (ExcelColumn) annotation;
                column.setFormatter(createDataFormatter(col.formatter()));
            }
            column.addAnnotation(annotation);
        }
        column.setMember(member);
        return column;
    }

    private static DataFormatter createDataFormatter(Class<? extends DataFormatter> formatter) {

        if (DataFormatter.class != formatter)
        {
            try
            {
                return formatter.newInstance();
            }
            catch (Throwable e)
            {
                throw new ExcelCreateException("can't create data formatter",e);
            }
        }
        return null;
    }


    private static Integer getFreeIndex(int index, ExcelColumnConf[] conf) {

        if (index < conf.length - 1 && conf[index] != null)
        {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }

}
