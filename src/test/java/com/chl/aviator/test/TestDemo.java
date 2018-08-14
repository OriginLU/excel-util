package com.chl.aviator.test;

import com.chl.excel.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
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
        Workbook excel = ExcelUtils.createExcel(arrayList, Demo.class);
        try {
            FileOutputStream outputStream = new FileOutputStream("D:/excel.xls");
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(excel);
        System.out.println(System.currentTimeMillis() - currentTimeMillis);
    }
}
