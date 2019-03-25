package com.chl.jdbc.extension.conf;

import com.chl.jdbc.extension.enums.SqlType;
import com.chl.jdbc.extension.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author lch
 * @since 2019-03-22
 */
public class SQLConfig {


    private SqlType sqlType;

    private String baseSQL;

    private ConditionConfig condition;

    private ConditionConfig refCondition;

    private Annotation sqlAnnotation;

    private String refName;

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public Annotation getSqlAnnotation() {
        return sqlAnnotation;
    }

    public void setSqlAnnotation(Annotation sqlAnnotation) {
        this.sqlAnnotation = sqlAnnotation;
    }

    public String getBaseSQL() {
        return baseSQL;
    }

    public void setBaseSQL(String baseSQL) {
        this.baseSQL = baseSQL;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }


    public ConditionConfig getRefCondition() {
        return refCondition;
    }

    public void setRefCondition(ConditionConfig refCondition) {
        this.refCondition = refCondition;
    }

    public ConditionConfig getCondition() {
        return condition;
    }

    public void setCondition(ConditionConfig condition) {
        this.condition = condition;
    }


    public String getSQL(Map<String,Object> env){


        StringBuilder conditionSQL = new StringBuilder();

        if (condition != null)
        {
            conditionSQL.append(condition.buildCondition(env));
        }

        if (refCondition != null)
        {
            conditionSQL.append(refCondition.buildCondition(env));
        }
        String condition = addConditionPrefix(conditionSQL.toString());

        return baseSQL + condition;
    }

    private String addConditionPrefix(String conditionString) {

        if (StringUtils.isBlank(conditionString))
        {
            return conditionString;
        }
        return " WHERE " + conditionString.substring(conditionString.indexOf("AND") + 3);
    }



}
