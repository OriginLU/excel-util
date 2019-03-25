package com.chl.aviator.test;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.exception.RepeatOrderException;
import com.chl.excel.util.JXLExcelUtils;
import com.chl.excel.util.POIExcelUtils;
import com.chl.excel.util.ReflectUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * @author lch
 * @since 2018-08-11
 */

public class TestDemo {


    @Test
    public void testExcelUtil(){

        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 60000; i++) {

            Demo demo = new Demo();
            demo.setName("Test" + i);
            demo.setId(i + "");
            arrayList.add(demo);
        }
        long currentTimeMillis = System.currentTimeMillis();
        Workbook excel = POIExcelUtils.createExcel(arrayList, Demo.class);
        try {
            FileOutputStream outputStream = new FileOutputStream("D:/test.xls");
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(excel);
        System.out.println(System.currentTimeMillis() - currentTimeMillis);
    }

    @Test
    public void jxlExcelUtilTest(){
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 60000; i++) {

            Demo demo = new Demo();
            demo.setName("Test jxlExcel" + i);
            demo.setId(i + "");
            arrayList.add(demo);
        }
        try {
            byte[] excel = JXLExcelUtils.createExcel(arrayList, Demo.class);
            FileOutputStream outputStream = new FileOutputStream("D:/test.xls");
            outputStream.write(excel);
            outputStream.flush();
            outputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Test
    public void getFieldTest(){

        System.out.println(Arrays.toString(ReflectUtils.getSpecifiedAnnotationFields(Demo.class, ExcelColumn.class).toArray()));
        System.out.println(Arrays.toString(ReflectUtils.getSpecifiedAnnotationMethods(Demo.class, ExcelColumn.class).toArray()));
        System.out.println(Arrays.toString(ExcelConfigureUtil.getExcelColConfiguration(Demo.class)));
        HashSet<Object> objects = new HashSet<>();
        System.out.println(objects.add(1));
        System.out.println(objects.add(1));
    }
     public static void main(String[] args){


        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 100000; i++) {

            Demo demo = new Demo();
            demo.setName("Test Demo" + i);
            demo.setId(i + "");
            arrayList.add(demo);
        }
        String excelAdvance = POIExcelUtils.createExcelFiles(arrayList, Demo.class,7000);
        System.out.println(excelAdvance);
    }


    @Test
    public void orderTest(){

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
                    if (!(hasOrder = orderSets.add(order))){
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

//                if (hasOrder && i != length - 1){
//                    System.out.println(i + " : " + getFreeIndex(i,conf));
//                    index.add(getFreeIndex(i,conf));
//                }

                if (!index.contains(order = getFreeIndex(i,conf)) && i != length - 1){
                    System.out.println(i + " : " + order );
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
