package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.entity.ExcelCol;
import com.chl.excel.exception.RepeatOrderException;
import com.chl.excel.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.*;
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

        for (int col = 0; col < length; col++) {

            Member member = members.get(col);
            ExcelCol columnConf = createExcelColumn(member);
            ExcelColumn excelColumn = ReflectUtils.getMemberAnnotation(member, ExcelColumn.class);
            Integer order = excelColumn.order();

            if (order > -1) {

                if (!orders.add(order)){
                    String memberName = member.getDeclaringClass().getName();
                    throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                            " in the member [" + memberName + "." + member.getName() + "]," + "which same as the" +
                            " member [" + memberName + "." + conf[order].getMember().getName() + "]");
                }

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

            if (!index.contains(order = getFreeIndex(col, conf)) && col != length - 1){
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


    public static void main(String[] args) {

        try {
            Set<Integer> orderSets = new HashSet();
            LinkedList<Integer> index = new LinkedList();
            List<Integer> list = Arrays.asList(0, -1, -1, 8, -1, 5, -1, 6, -1);
            Integer[] conf = new Integer[list.size()];
            long start = System.currentTimeMillis();
            boolean hasOrder = false;
            for (int i = 0, length = list.size(); i < length; i++) {

                Integer order = list.get(i);
                if (order > -1) {
                    if (!orderSets.add(order)){
                        throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order);
                    }
                } else {
                    order = (index.size() > 0) ? index.pop() : getFreeIndex(i, conf);
                }
                if (conf[order] != null) {
                    Integer tempIndex = (index.size() > 0) ? index.pop() : getFreeIndex(i, conf);
                    Integer temp = conf[order];
                    conf[order] = order;
                    conf[tempIndex] = tempIndex;
                } else {
                    conf[order] = order;
                }

                if (!index.contains(order = getFreeIndex(i, conf)) && i != length - 1){
                    index.add(order);
                }
            }
            System.out.println("time : " + (System.currentTimeMillis() - start));
            System.out.println(Arrays.toString(conf));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Integer getFreeIndex(Integer index, Integer[] conf) {

        if (index < conf.length - 1 && conf[index] != null) {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }
}
