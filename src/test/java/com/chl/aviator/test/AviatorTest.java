package com.chl.aviator.test;

import com.chl.excel.util.ReflectUtils;
import com.googlecode.aviator.AviatorEvaluator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

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
        System.out.println(AviatorEvaluator.exec("demo.name",demo));
    }

    @Test
    public void testReflect(){

        Demo demo = new Demo();
        demo.setName(UUID.randomUUID().toString());
        try {
            System.out.println(ReflectUtils.invokeMethod(demo, "setName", UUID.randomUUID().toString()));
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



        new Thread(new Runnable() {
            @Override
            public void run() {
                LockSupport.park();
            }
        },"Waiting Thread").start();
        System.out.println("yield before ....");
        Thread.yield();
        System.out.println("yield after .....");
    }

}

