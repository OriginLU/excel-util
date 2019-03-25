package com.chl.jdbc.extension.parse;

import com.chl.excel.util.ReflectUtils;
import com.chl.jdbc.extension.annotation.*;
import com.chl.jdbc.extension.conf.ConditionConfig;
import com.chl.jdbc.extension.conf.SQLConfig;
import com.chl.jdbc.extension.context.SQLContext;
import com.chl.jdbc.extension.enums.SqlType;
import com.chl.jdbc.extension.exception.NotFoundConfiguration;
import com.chl.jdbc.extension.expression.AviatorFunctionRegister;
import com.chl.jdbc.extension.expression.ExpressionConstants;
import com.chl.jdbc.extension.expression.function.BlankFunction;
import com.chl.jdbc.extension.expression.function.NullFunction;
import com.chl.jdbc.extension.util.CollectionUtils;
import com.chl.jdbc.extension.util.StringUtils;
import com.chl.jdbc.extension.util.ThreadContext;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lch
 * @since 2019-03-22
 */
public class AnnotationSQLParser {

    public AnnotationSQLParser() {

        initial();
    }

    private void initial() {

        AviatorFunctionRegister.register(new NullFunction());
        AviatorFunctionRegister.register(new BlankFunction());
    }


    public void parse(Class clazz,SQLContext context){

        try
        {
            List<Method> methods = ReflectUtils.getMethods(clazz);

            if (!CollectionUtils.isEmpty(methods))
            {
                List<SQLConfig> completeList = new ArrayList<>();

                for (Method method : methods)
                {
                    parse(method,context,completeList);
                }
                completedSQLConfig(completeList);
            }
        }
        finally
        {
            ThreadContext.clear();
        }
    }


    private void parse(Method method, SQLContext sqlContext,List<SQLConfig> completedList){


        String name = method.getDeclaringClass().getName() + "." + method.getName();

        Annotation[] annotations = method.getDeclaredAnnotations();

        if (!CollectionUtils.isEmpty(annotations))
        {
            SQLConfig sqlConfig = null;
            ConditionConfig conditionConfig = null;
            for (Annotation annotation : annotations)
            {
                Class<? extends Annotation> annotationType = annotation.annotationType();

                if (annotationType == Condition.class)
                {
                    conditionConfig = createConditionConf(name,(Condition)annotation);
                }
                else if (annotationType == Conditions.class)
                {
                    conditionConfig = createConditionsConf(name,(Conditions)annotation);
                }
                else if (annotationType == Insert.class)
                {
                    Insert insert = (Insert) annotation;
                    sqlConfig = createSQLConfig(name,annotation,insert.baseSQL(),insert.refCondition(),sqlContext, SqlType.INSERT);
                }
                else if (annotationType == Update.class)
                {
                    Update update = (Update) annotation;
                    sqlConfig = createSQLConfig(name,annotation, update.baseSQL(), update.refCondition(), sqlContext, SqlType.UPDATE);
                }
                else if (annotationType == Delete.class)
                {
                    Delete delete = (Delete) annotation;
                    sqlConfig = createSQLConfig(name,annotation, delete.baseSQL(), delete.refCondition(), sqlContext, SqlType.DELETE);
                }
                else if (annotationType == Select.class)
                {
                    Select select = (Select) annotation;
                    sqlConfig = createSQLConfig(name,annotation, select.baseSQL(), select.refCondition(), sqlContext, SqlType.SELECT);
                }
            }

            if (sqlConfig != null)
            {
                sqlConfig.setCondition(conditionConfig);

                if (StringUtils.isNotBlank(sqlConfig.getRefName()))
                {
                    completedList.add(sqlConfig);
                }
            }
        }

    }

    private void completedSQLConfig(List<SQLConfig> completeList) {


        if (!CollectionUtils.isEmpty(completeList))
        {
            for (SQLConfig sqlConfig : completeList) {

                String refName = sqlConfig.getRefName();
                ConditionConfig refCondition = (ConditionConfig) ThreadContext.getValue(refName);
                if (refCondition == null)
                {
                    throw new NotFoundConfiguration("not found reference [" + refName + "]");
                }
                sqlConfig.setRefCondition(refCondition);
            }
        }
    }

    private SQLConfig createSQLConfig(String name, Annotation annotation, String sql, String refs, SQLContext sqlContext, SqlType sqlType) {

        SQLConfig config = sqlContext.getSQLConfig(name);
        if (config == null){

            if (StringUtils.isBlank(sql))
            {
                throw new NotFoundConfiguration("[" + name + "] sql is blank,please check");
            }

            SQLConfig sqlConfig = new SQLConfig();

            sqlConfig.setBaseSQL(sql);
            sqlConfig.setSqlAnnotation(annotation);
            sqlConfig.setSqlType(sqlType);
            sqlConfig.setRefName(refs);

            sqlContext.addSQLConfig(name,sqlConfig);
            return sqlConfig;
        }
        return config;
    }

    private ConditionConfig createConditionsConf(String name, Conditions annotation) {


        ConditionConfig condConfig = (ConditionConfig) ThreadContext.getValue(name);
        if (condConfig == null)
        {
            Condition[] conditions = annotation.value();

            String conditionName = annotation.name();

            if (conditions.length > 0)
            {
                List<String> cons = new ArrayList<>();
                List<String> exps = new ArrayList<>();
                List<Expression> invokers = new ArrayList<>();
                ConditionConfig conditionConfig = new ConditionConfig();


                for (Condition cond : conditions)
                {
                    String condition = cond.condition();
                    String expression = cond.expression();

                    if (StringUtils.isNotBlank(condition))
                    {
                        cons.add(removeAnd(condition));
                        if (StringUtils.isBlank(expression))
                        {
                            exps.add(ExpressionConstants.TRUE);
                            invokers.add(AviatorEvaluator.compile(ExpressionConstants.TRUE));
                        }
                        else
                        {
                            exps.add(expression);
                            invokers.add(AviatorEvaluator.compile(expression));
                        }
                    }
                }
                conditionConfig.setName(annotation.name());
                conditionConfig.setCondAnnotation(annotation);
                conditionConfig.setInvokers(invokers.toArray(new Expression[0]));
                conditionConfig.setConditions(cons.toArray(new String[0]));
                conditionConfig.setExpressions(exps.toArray(new String[0]));

                if (StringUtils.isNotBlank(conditionName))
                {
                    ThreadContext.put(conditionName,conditionConfig);
                }
                ThreadContext.put(name,conditionConfig);
                return conditionConfig;
            }
            return null;
        }

        return condConfig;
    }

    private ConditionConfig createConditionConf(String name, Condition annotation) {


        ConditionConfig condConfig = (ConditionConfig) ThreadContext.getValue(name);
        if (condConfig == null)
        {
            String condition = annotation.condition();
            String expression = annotation.expression();
            String conditionName = annotation.name();

            if (StringUtils.isNotBlank(condition))
            {
                ConditionConfig conditionConfig = new ConditionConfig();

                conditionConfig.setCondAnnotation(annotation);
                conditionConfig.setConditions(new String[]{removeAnd(condition)});
                if (StringUtils.isBlank(expression))
                {
                    conditionConfig.setExpressions(new String[]{ExpressionConstants.TRUE});
                    conditionConfig.setInvokers(new Expression[]{AviatorEvaluator.compile(ExpressionConstants.TRUE)});
                }
                else
                {
                    conditionConfig.setExpressions(new String[]{expression});
                    conditionConfig.setInvokers(new Expression[]{AviatorEvaluator.compile(expression)});
                }

                ThreadContext.put(name,conditionConfig);
                if (StringUtils.isNotBlank(conditionName))
                {
                    ThreadContext.put(conditionName,conditionConfig);
                }
                return conditionConfig;
            }
            return null;
        }
        return condConfig;
    }

    private String removeAnd(String condition){

         condition = condition.trim();

        String cond = condition.toLowerCase();

        if (cond.startsWith("and")) {

            return condition.substring(cond.indexOf("and") + 3);
        }
        return condition;
    }
}
