package com.chl.aviator.test;


import com.chl.jdbc.extension.conf.SQLConfig;
import com.chl.jdbc.extension.context.SQLContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2018-09-11
 */
public class Main {


    public static void main(String[] args)  {


        SQLContext context = SQLContext.getInstance();

        SQLConfig sqlConfig = context.getSQLConfig("com.chl.aviator.test.TestSQL.testDemo()", TestSQL.class);

        Map<String, Object> paraMap = new HashMap<>();

//        paraMap.put("startTime",new Date());

        System.out.println(sqlConfig.getSQL(paraMap));

    }




}
