package com.chl.excel.entity;

import com.chl.excel.formatter.DataFormatter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2018-08-16
 */
public class ExcelColumnConf {

    private Map<Class,Annotation> annotations;

    private Member member;

    private DataFormatter formatter;


    public DataFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DataFormatter formatter) {
        this.formatter = formatter;
    }

    public Map<Class, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<Class, Annotation> annotations) {
        this.annotations = annotations;
    }

    public Member  getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void addAnnotation(Annotation ans){

        if (this.annotations == null){
            this.annotations = new HashMap<>();
        }
        this.annotations.put(ans.annotationType(),ans);
    }

}
