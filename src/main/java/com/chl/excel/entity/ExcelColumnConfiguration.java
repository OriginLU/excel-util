package com.chl.excel.entity;

import com.chl.excel.formatter.DataFormatter;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2018-08-16
 */
public class ExcelColumnConfiguration {



    private String columnName;

    private Field field;

    private Method method;

    private DataFormatter formatter;

    private TypeDescriptor typeDescriptor;

    private Map<Class,Annotation> annotations;


    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    public void setTypeDescriptor(TypeDescriptor typeDescriptor) {
        this.typeDescriptor = typeDescriptor;
    }

    public DataFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DataFormatter formatter) {
        this.formatter = formatter;
    }

    public Map<Class, Annotation> getAnnotations() {
        return annotations;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }


    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void addAnnotation(Annotation ans){

        if (this.annotations == null){
            this.annotations = new HashMap<>();
        }
        this.annotations.put(ans.annotationType(),ans);
    }

}
