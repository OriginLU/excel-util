package com.chl.excel.configure.impl;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelColumnConfiguration;
import com.chl.excel.util.ReflectUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public class FieldConfigurationLoader extends AbstractConfigurationLoader<Field> {


    @Override
    protected TypeDescriptor createTypeDescriptor(Field member) {
        return new TypeDescriptor(member);
    }

    @Override
    protected Annotation[] getAnnotations(Field member) {

        return member.getAnnotations();
    }

    @Override
    protected void setMember(ExcelColumnConfiguration column, Field member) {

        column.setField(member);

    }

    @Override
    protected List<Field> getMember(Class<?> clazz) {

        return ReflectUtils.getSpecifiedAnnotationFields(clazz, ExcelColumn.class);
    }



}
