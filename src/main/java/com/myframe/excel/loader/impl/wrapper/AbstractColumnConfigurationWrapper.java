package com.myframe.excel.loader.impl.wrapper;

import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.formatter.DataFormatter;
import com.myframe.excel.loader.ColumnConfigurationWrapper;
import com.myframe.excel.support.spring.AutowireDataFormatterBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

public abstract class AbstractColumnConfigurationWrapper<T extends Member> implements ColumnConfigurationWrapper<T> {



    public ExcelColumnConfiguration createExcelColumnConfiguration(T member) {

        ExcelColumnConfiguration column = new ExcelColumnConfiguration();

        Annotation[] annotations = getAnnotations(member);
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType() == ExcelColumn.class)
            {
                ExcelColumn col = (ExcelColumn) annotation;

                Class<?> formatter = getDataFormatterClass(col);
                column.setOrder(col.order());
                column.setDefaultValue(col.value());
                column.setColumnName(getColumnName(member,col));
                column.setFormatter(createDataFormatter(formatter));
            }
            column.addAnnotation(annotation);
        }
        setMember(column,member);

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

    private DataFormatter createDataFormatter(Class<?> formatter) {

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

    protected abstract TypeDescriptor createTypeDescriptor(T member);

    protected abstract Annotation[] getAnnotations(T member);

    protected abstract void setMember(ExcelColumnConfiguration column, T member);
}
