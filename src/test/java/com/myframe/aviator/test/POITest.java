package com.myframe.aviator.test;

import com.myframe.excel.entity.ExcelConfiguration;
import com.myframe.excel.loader.ConfigurationLoader;
import com.myframe.excel.loader.impl.conf.ExcelConfigurationLoader;
import com.myframe.excel.poi.POIFactory;
import com.myframe.excel.poi.support.ExcelOperationService;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LCH
 * @since 2018-06-15
 */
public class POITest {


    public static void main(String[] args){


        ExecutorService executorService = Executors.newCachedThreadPool();

        POITest POITest = new POITest();

        for (int i = 0; i < 5; i++)
        {
            String name = "test-" + i + ".xls";
            executorService.execute(() -> POITest.poiTest(name));
        }
        executorService.shutdown();
    }

    @Test
    public void importExcel(){

        try {
            ExcelOperationService excelService = POIFactory.getInstance().build();
            String path = getClass().getResource("/").getPath();
            List<?> objects = excelService.importData(new FileInputStream(path + "test-0.xls"), Demo.class);
            System.out.println(objects);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    private void poiTest(String name){


        List<Demo> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 50000; i++) {

            Demo demo = new Demo();

            demo.setName("Test_" + i);
            demo.setCreateTime(new Date());
            demo.setCode("12346" + random.nextInt(10000));

            demo.setId(random.nextDouble() + "");
            demo.setStatus("1");
            demo.setTime(System.currentTimeMillis()+ "");

            list.add(demo);
        }
        try
        {
            long currentTimeMillis = System.currentTimeMillis();

            ExcelOperationService excelService = POIFactory.getInstance().build();
            Workbook excel = excelService.exportSheet(list, Demo.class);


            String path = getClass().getResource("/").getPath();
            File file = new File(path + name);
            System.out.println("out path is [" + file.getPath() + "]");

            FileOutputStream outputStream = new FileOutputStream(new File(path + name));

            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            System.out.println("cost time : " + (System.currentTimeMillis() - currentTimeMillis));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void testExcelConfigurationLoader(){

        long currentTimeMillis = System.currentTimeMillis();

        ConfigurationLoader configurationLoader = ExcelConfigurationLoader.getExcelConfigurationLoader();
        configurationLoader.getExportConfiguration(Demo.class);

        System.out.println("load time :" + (System.currentTimeMillis() - currentTimeMillis));

    }

}

