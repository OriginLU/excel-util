package com.chl.aviator.test;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.util.JXLExcelUtils;
import com.chl.excel.util.POIExcelUtils;
import com.chl.excel.util.ReflectUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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
}
