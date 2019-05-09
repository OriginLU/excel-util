package com.myframe.excel.loader.impl;

import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.util.ReflectUtils;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class MethodConfigurationLoader extends AbstractConfigurationLoader<Method> {


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
    protected List<Method> getMember(Class<?> clazz) {

        return ReflectUtils.getSpecifiedAnnotationMethods(clazz, ExcelColumn.class);
    }

    @Override
    protected String getColumnName(Method member, ExcelColumn excelColumn) {
        return super.getColumnName(member, excelColumn);
    }
}
