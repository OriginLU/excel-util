package com.chl.excel.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2018-08-16
 */
public class ExcelCol {

    private Map<Class,Annotation> annotations;

    private Member member;

    public Map<Class, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<Class, Annotation> annotations) {
        this.annotations = annotations;
    }

    public <T extends Member> T getMember() {
        return (T) member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void addAnnotation(Annotation ans){

        if (this.annotations == null){
            this.annotations = new HashMap();
        }
        this.annotations.put(ans.annotationType(),ans);
    }

    @Override
    public String toString() {
        return "ExcelCol{" +
                "annotations=" + annotations +
                ", member=" + member +
                '}';
    }
}
