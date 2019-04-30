package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelColumnConfiguration;
import com.chl.excel.exception.ExcelCreateException;
import com.chl.excel.exception.RepeatOrderException;
import com.chl.excel.formatter.DataFormatter;
import com.chl.excel.util.ReflectUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    public static ExcelColumnConfiguration[] getExcelColumnConfiguration(List<Field> members){


        int length = members.size();
        Set<Integer> orders = new HashSet<>();
        ExcelColumnConfiguration[] conf = new ExcelColumnConfiguration[length];
        LinkedList<Integer> index = new LinkedList<>();

        for (int col = 0; col < length; col++)
        {
            Field member = members.get(col);
            ExcelColumnConfiguration columnConf = createExcelColumnConf(member);
            ExcelColumn excelColumn = ReflectUtils.getMemberAnnotation(member, ExcelColumn.class);
            Integer order = excelColumn.order();

            if (order > -1)
            {
                if (!orders.add(order))
                {
                    String memberName = member.getDeclaringClass().getName();
                    throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                            " in the member [" + memberName + "." + member.getName() + "]," + "which same as the" +
                            " member [" + memberName + "." + conf[order].getField().getName() + "]");
                }
            }
            else
            {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
            }

            if (conf[order] != null)
            {
                Integer tempIndex = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
                ExcelColumnConfiguration temp = conf[order];
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


    private static ExcelColumnConfiguration createExcelColumnConf(Field member) {

        ExcelColumnConfiguration column = new ExcelColumnConfiguration();
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
        column.setTypeDescriptor(createTypeDescriptor(member));
        column.setField(member);
        return column;
    }

    private static TypeDescriptor createTypeDescriptor(Field field) {

        return new TypeDescriptor(field);
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


    private static Integer getFreeIndex(int index, ExcelColumnConfiguration[] conf) {

        if (index < conf.length - 1 && conf[index] != null)
        {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }

}
