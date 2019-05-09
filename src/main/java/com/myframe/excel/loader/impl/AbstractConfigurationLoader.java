package com.myframe.excel.loader.impl;

import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.loader.ConfigurationLoader;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.exception.RepeatOrderException;
import com.myframe.excel.formatter.DataFormatter;
import com.myframe.excel.support.spring.AutowireDataFormatterBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbstractConfigurationLoader<T extends Member> implements ConfigurationLoader {


    public ExcelColumnConfiguration[] getExcelColumnConfiguration(Class<?> clazz,ExcelColumnConfiguration[] conf){


        List<T> members = getMember(clazz);
        int length = members.size();
        Set<Integer> orders = new HashSet<>();
        LinkedList<Integer> index = new LinkedList<>();

        for (int col = 0; col < length; col++)
        {
            T member = members.get(col);
            ExcelColumnConfiguration columnConf = createExcelColumnConf(member);
            int order = getOrder(columnConf);

            if (order > -1)
            {
                if (order >= length)
                {
                    throw new IndexOutOfBoundsException("the specified index [" + order + "] out of bound,max length is " + length);
                }
                if (!orders.add(order))
                {
                    String memberName = member.getDeclaringClass().getName();
                    throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                            " in the member [" + memberName + "." + member.getName() + "]," + "which same as the" +
                            " member [" + memberName + "." + getName(conf[order]) + "]");
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

    private String getName(ExcelColumnConfiguration configuration){

        return configuration.getField() == null ? configuration.getMethod().getName() : configuration.getField().getName();
    }

    private Integer getOrder(ExcelColumnConfiguration conf){

        return ((ExcelColumn)conf.getAnnotations().get(ExcelColumn.class)).order();
    }


    private ExcelColumnConfiguration createExcelColumnConf(T member) {

        ExcelColumnConfiguration column = new ExcelColumnConfiguration();

        setMember(column,member);
        Annotation[] annotations = getAnnotations(member);
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType() == ExcelColumn.class)
            {
                ExcelColumn col = (ExcelColumn) annotation;

                Class<?> formatter = getDataFormatterClass(col);
                column.setDefaultValue(col.value());
                column.setColumnName(getColumnName(member,col));
                column.setFormatter(createDataFormatter(formatter));
            }
            column.addAnnotation(annotation);
        }
        column.setTypeDescriptor(createTypeDescriptor(member));

        return column;
    }

    private Class<?> getDataFormatterClass(ExcelColumn col){

        try
        {
            String formatterClassName = col.formatterClassName();

            if (StringUtils.isNotBlank(formatterClassName))
            {
                return Class.forName(formatterClassName);
            }
            return col.formatter();
        }
        catch (Throwable e)
        {
            throw new ExcelCreateException("the data formatter load occur error",e);
        }

    }

    protected String getColumnName(T member,ExcelColumn excelColumn){

        String columnName = excelColumn.columnName();

        if (StringUtils.isBlank(columnName))
        {
            return member.getName();
        }
        return columnName;
    }

    private  DataFormatter createDataFormatter(Class<?> formatter) {

        if (DataFormatter.class != formatter)
        {
            try
            {
                return (DataFormatter) AutowireDataFormatterBeanFactory.autowireBean(formatter.newInstance());
            }
            catch (Throwable e)
            {
                throw new ExcelCreateException("can't create data formatter",e);
            }
        }
        return null;
    }


    private Integer getFreeIndex(int index, ExcelColumnConfiguration[] conf) {

        if (index < conf.length - 1 && conf[index] != null)
        {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }


    protected abstract TypeDescriptor createTypeDescriptor(T member);

    protected abstract Annotation[] getAnnotations(T member);

    protected abstract void setMember(ExcelColumnConfiguration column, T member);

    protected abstract List<T> getMember(Class<?> clazz);
}
