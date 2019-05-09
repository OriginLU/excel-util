package com.myframe.excel.loader.impl.wrapper;

import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodConfigurationWrapper extends AbstractColumnConfigurationWrapper<Method> {


    @Override
    protected TypeDescriptor createTypeDescriptor(Method member) {

        Class<?> returnType = member.getReturnType();

        if (returnType == void.class)
        {
            throw new ExcelCreateException("return type not null be " + void.class.getName());
        }

        Property property = new Property(returnType, member, null);

        return new TypeDescriptor(property);
    }

    @Override
    protected Annotation[] getAnnotations(Method member) {

        return member.getAnnotations();
    }

    @Override
    protected void setMember(ExcelColumnConfiguration column, Method member) {

        column.setMethod(member);
    }

    @Override
    protected String getColumnName(Method member, ExcelColumn excelColumn) {
        return super.getColumnName(member, excelColumn);
    }
}
