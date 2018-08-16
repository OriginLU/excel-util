package com.chl.aviator.test;

import com.chl.excel.util.JXLExcelUtils;
import com.chl.excel.util.POIExcelUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

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
        for (int i = 0; i < 6000; i++) {

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
