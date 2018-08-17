package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelCol;
import com.chl.excel.exception.RepeatOrderException;
import com.chl.excel.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 获取excel配置信息
 *
 * @author LCH
 * @since 2018-07-02
 */
public abstract class ExcelConfigureUtil {


    private static ConcurrentMap<Class, ExcelCol[]> excelConf = new ConcurrentHashMap();


    public static ExcelCol[] getExcelColConfiguration(Class clazz) {

        ExcelCol[] conf = excelConf.get(clazz);
        if (conf != null) {
            return conf;
        }
        List members = ReflectUtils.getSpecifiedAnnotationFields(clazz, ExcelColumn.class);
        members.addAll(ReflectUtils.getSpecifiedAnnotationMethods(clazz, ExcelColumn.class));
        conf = getExcelColumnArray(clazz, members);
        excelConf.putIfAbsent(clazz, conf);
        return conf;

    }

    public static String getExcelTitleName(Class type) {

        Excel annotation = (Excel) type.getAnnotation(Excel.class);
        return annotation.value();
    }

    public static String getExcelVersion(Class type) {

        Excel annotation = (Excel) type.getAnnotation(Excel.class);
        return annotation.version();
    }

    private static ExcelCol[] getExcelColumnArray(Class clazz, List<Member> members) throws RepeatOrderException {


        int length = members.size();
        Set<Integer> orders = new HashSet();
        ExcelCol[] conf = new ExcelCol[length];
        LinkedList<Integer> index = new LinkedList();

        for (int col = 0; col < length; col ++) {

            Member member = members.get(col);
            ExcelCol columnConf = createExcelColumn(member);
            ExcelColumn excelColumn = ReflectUtils.getMemberAnnotation(member, ExcelColumn.class);
            Integer order = excelColumn.order();

            if (order > -1 && !orders.add(order)) {

                String memberName = member.getDeclaringClass().getName();
                throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                        " in the member [" + memberName + "." + member.getName() + "]," + "which same as the" +
                        " member [" + memberName + "." + conf[order].getMember().getName() + "]");

            } else {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
            }

            if (conf[order] != null) {
                Integer tempIndex = (index.size() > 0) ? index.pop() : getFreeIndex(col, conf);
                ExcelCol temp = conf[order];
                conf[order] = columnConf;
                conf[tempIndex] = temp;
            } else {
                conf[order] = columnConf;
            }

            if (!index.contains(order = getFreeIndex(col, conf)) && col != length - 1) {
                index.add(order);
            }
        }
        return conf;
    }


    private static ExcelCol createExcelColumn(Member member) {

        ExcelCol column = new ExcelCol();
        Annotation[] annotations = ReflectUtils.getMemberAnnotations(member);
        for (Annotation annotation : annotations) {
            column.addAnnotation(annotation);
        }
        column.setMember(member);
        return column;
    }


    private static Integer getFreeIndex(int index, ExcelCol[] conf) {

        if (index < conf.length - 1 && conf[index] != null) {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }
}
