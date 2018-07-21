package com.chl.excel.configure;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.RepeatOrderExcetion;
import com.chl.excel.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 获取excel配置信息
 * @author LCH
 * @since 2018-07-02
 */
public abstract class ExcelConfigureUtil {


    public static ExcelColumnConf[] getExcelColumnConfiguration(Class clazz) throws RepeatOrderExcetion {

        List<Field> fields = ReflectUtils.getSpecifiedAnnotationFields(clazz, ExcelColumn.class);
        List<Method> methods = ReflectUtils.getSpecifiedAnnotationMethods(clazz, ExcelColumn.class);
        ExcelColumnConf[] array = new ExcelColumnConf[fields.size() + methods.size()];
        array = getExcelColumnFieldArray(clazz, fields, array);
        array = getExcelColumnMethodArray(clazz, methods, array, fields.size());
        return array;

    }

    private static ExcelColumnConf[] getExcelColumnMethodArray(Class clazz, List<Method> methods, ExcelColumnConf[] conf, Integer startIndex) throws RepeatOrderExcetion {


        Set<Integer> orderSets = new HashSet();
        LinkedList<Integer> index = new LinkedList();
        for (int i = 0, length = methods.size(), currIndex = startIndex; i < length; i++) {
            Method method = methods.get(i);
            ExcelColumnConf columnConf = createExcelColumnConf(method);
            ExcelColumn excelColumn = method.getAnnotation(ExcelColumn.class);
            Integer order = excelColumn.order();
            currIndex = i + startIndex;
            if (order > -1) {
                if (orderSets.contains(order)) {
                    throw new RepeatOrderExcetion("the order must not be repeated, the repeat order is " + order + "in the method [" + method.getName() + "]"
                            + "which same as " + conf[order].getAnnotationField() != null ? "field [" + conf[order].getAnnotationField().getName() + "]" : "method [" + conf[order].getAnnotationMethod().getName() + "]");
                }
                orderSets.add(order);
            } else {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(currIndex, conf);
            }
            if (conf[order] != null) {
                Integer tempIndex = (index.size() != 0 && (tempIndex = index.pop()) != null) ? tempIndex : getFreeIndex(currIndex, conf);
                ExcelColumnConf temp = conf[order];
                conf[order] = columnConf;
                conf[tempIndex] = temp;
            } else {
                conf[order] = columnConf;
            }
            if (!index.contains(order = getFreeIndex(currIndex,conf)) && i != length - 1)
                index.add(order);
        }
        return conf;
    }

    private static ExcelColumnConf[] getExcelColumnFieldArray(Class clazz, List<Field> fields, ExcelColumnConf[] conf) throws RepeatOrderExcetion {


        Set<Integer> orderSets = new HashSet();
        LinkedList<Integer> index = new LinkedList();
        for (int i = 0, length = fields.size(); i < length; i++) {
            Field field = fields.get(i);
            ExcelColumnConf columnConf = createExcelColumnConf(field);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            Integer order = excelColumn.order();
            if (order > -1) {
                if (orderSets.contains(order)) {
                    throw new RepeatOrderExcetion("the order must not be repeated, the repeat order is " + order
                            + "in the field [" + field.getName() + "],which same as the field [" + conf[order].getAnnotationField().getName() + "]");
                }
                orderSets.add(order);
            } else {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(i, conf);
            }
            if (conf[order] != null) {
                Integer tempIndex = (index.size() != 0 && (tempIndex = index.pop()) != null) ? tempIndex : getFreeIndex(i, conf);
                ExcelColumnConf temp = conf[order];
                conf[order] = columnConf;
                conf[tempIndex] = temp;
            } else {
                conf[order] = columnConf;
            }
            if (!index.contains(order = getFreeIndex(i, conf)) && i != length - 1)
                index.add(order);
        }
        return conf;
    }

    private static ExcelColumnConf createExcelColumnConf(Field field) {

        ExcelColumnConf columnConf = new ExcelColumnConf();
        columnConf.setAnnotationField(field);
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            columnConf.addAnnotation(annotation);
        }
        return columnConf;
    }

    private static ExcelColumnConf createExcelColumnConf(Method method) {

        ExcelColumnConf columnConf = new ExcelColumnConf();
        columnConf.setAnnotationMethod(method);
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            columnConf.addAnnotation(annotation);
        }
        return columnConf;
    }

    private static Integer getFreeIndex(int index, ExcelColumnConf[] conf) {

        if (index < conf.length - 1 && conf[index] != null) {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }
}
