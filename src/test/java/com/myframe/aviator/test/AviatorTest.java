package com.myframe.aviator.test;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.myframe.excel.poi.POIFactory;
import com.myframe.excel.poi.support.ExcelOperationService;
import com.myframe.excel.util.ReflectionUtils;
import com.myframe.jdbc.extension.expression.function.BlankFunction;
import com.myframe.jdbc.extension.expression.function.NullFunction;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LCH
 * @since 2018-06-15
 */
public class AviatorTest {



    @Test
    public void testAviator(){

        System.out.println(AviatorEvaluator.execute("1.2000+3+4").toString());
    }

    @Test
    public void testAviatorContext(){

        Demo demo = new Demo();
        demo.setId(UUID.randomUUID().toString());
        demo.setName("AviatorContext");

        Expression expression = AviatorEvaluator.compile("demo.name != ''");

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("demo",demo);
        boolean execute = (boolean) expression.execute(paraMap);
        System.out.println(execute);
    }

    @Test
    public void nullableTest(){


            AviatorEvaluator.addFunction(new NullFunction());
            AviatorEvaluator.addFunction(new BlankFunction());

            Expression expression = AviatorEvaluator.compile("isBlank(name)");

            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("name","123456");
            boolean execute = (boolean) expression.execute(paraMap);
            System.out.println(execute);

    }

    @Test
    public void testReflect(){

        Demo demo = new Demo();
        demo.setName(UUID.randomUUID().toString());
        try {
            System.out.println(ReflectionUtils.invokeMethod(demo, "setName", UUID.randomUUID().toString()));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test public void testTimestamp(){

        System.out.println("2018-12-12 00:00:00 hex : " + Long.toHexString(getDate("2018-12-12 00:00:00")));
        System.out.println("2018-12-12 00:00:00 bin : " + Long.toBinaryString(getDate("2018-12-12 00:00:00")));

        System.out.println("2086-12-12 00:00:00 hex : " + Long.toHexString(getDate("2068-12-12 00:00:00")));
        System.out.println("2086-12-12 00:00:00 bin : " + Long.toBinaryString(getDate("2068-12-12 00:00:00")));
        long    temp = getDate("2087-12-12 00:00:00") - getDate("2018-12-12 00:00:00");
        System.out.println(Long.toBinaryString(temp));
    }

    private Long getDate(String time){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parse = format.parse(time);
            return parse.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args){


        ExecutorService executorService = Executors.newCachedThreadPool();

        AviatorTest aviatorTest = new AviatorTest();

        for (int i = 0; i < 5; i++) {

            final int index = i;
            executorService.execute(() -> {
                aviatorTest.poiTest("test_" + index  + ".xls");

            });
        }
        executorService.shutdown();
    }

    @Test
    public void importExcel(){

        try {
            ExcelOperationService excelService = POIFactory.getInstance().build();
            List<?> objects = excelService.importData(new FileInputStream("E:\\git\\frame-util\\target\\test_0.xls"), Demo.class);

            System.out.println(objects);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void poiTest(String name){


        List<Demo> list = new ArrayList<>();


        Random random = new Random();

        for (int i = 0; i < 10000; i++) {

            Demo demo = new Demo();

            demo.setName("Test_" + i);
            demo.setCreateTime(new Date());
            demo.setCode("12346" + random.nextInt(10000));

            demo.setId(random.nextDouble() + "");
            demo.setStatus("1");
            demo.setTime(System.currentTimeMillis()+ "");

            list.add(demo);
        }



        try {
            System.out.println("start.......");
            long currentTimeMillis = System.currentTimeMillis();

            ExcelOperationService excelService = POIFactory.getInstance().build();
            Workbook excel = excelService.exportSheet(list, Demo.class);
            FileOutputStream outputStream = new FileOutputStream(new File("E:\\git\\frame-util\\target\\" + name));

            excel.write(outputStream);

            outputStream.flush();
            outputStream.close();
            System.out.println(System.currentTimeMillis() - currentTimeMillis);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

