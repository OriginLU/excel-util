package com.chl.aviator.test;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.exception.RepeatOrderException;
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
