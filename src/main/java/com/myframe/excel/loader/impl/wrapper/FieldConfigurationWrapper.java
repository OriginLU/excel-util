package com.myframe.excel.loader.impl.wrapper;

import com.myframe.excel.entity.ExcelColumnConfiguration;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FieldConfigurationWrapper extends AbstractColumnConfigurationWrapper<Field>{


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


}
