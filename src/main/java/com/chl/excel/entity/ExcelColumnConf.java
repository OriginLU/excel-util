package com.chl.excel.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LCH
 * @since 2018-06-29
 */
public class ExcelColumnConf{


    private Map<Class,Annotation> annotations;

    private Field annotationField;

    private Method annotationMethod;


    public Field getAnnotationField() {
        return annotationField;
    }

    public void setAnnotationField(Field annotationField) {
        this.annotationField = annotationField;
    }

    public Method getAnnotationMethod() {
        return annotationMethod;
    }

    public void setAnnotationMethod(Method annotationMethod) {
        this.annotationMethod = annotationMethod;
    }

    public Map<Class, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotation(Map<Class, Annotation> annotations) {
        this.annotations = annotations;
    }

    public void addAnnotation(Annotation ans){
        if (this.annotations == null){
            this.annotations = new HashMap();
        }
        this.annotations.put(ans.annotationType(),ans);
    }
}
