package com.chl.jdbc.extension.conf;

import com.googlecode.aviator.Expression;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author lch
 * @since 2019-03-23
 */
public class ConditionConfig {


    private String name;

    private String[] expressions;

    private String[] conditions;

    private Expression[] invokers;

    private Annotation condAnnotation;

    public Annotation getCondAnnotation() {
        return condAnnotation;
    }

    public void setCondAnnotation(Annotation condAnnotation) {
        this.condAnnotation = condAnnotation;
    }

    public void setExpressions(String[] expressions) {
        this.expressions = expressions;
    }

    public void setConditions(String[] conditions) {
        this.conditions = conditions;
    }

    public void setInvokers(Expression[] invokers) {
        this.invokers = invokers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String buildCondition(Map<String,Object> env){

        StringBuilder conditionSQL = new StringBuilder();
        if (invokers != null && invokers.length > 0)
        {
            for (int i = 0; i < invokers.length; i++)
            {
                Expression invoker = invokers[i];
                boolean required = (boolean) invoker.execute(env);
                conditionSQL.append(assemblyCondition(required ? conditions[i] : ""));
            }

        }

        return conditionSQL.toString();
    }


    private String assemblyCondition(String condition){

        if ("".equals(condition))
        {
            return  condition;
        }
        return " AND " + condition;
    }
}
