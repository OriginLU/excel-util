package com.chl.aviator.test;


import com.chl.jdbc.extension.conf.SQLConfig;
import com.chl.jdbc.extension.context.SQLContext;
import com.chl.jdbc.extension.proxy.ProxyFactory;

import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author lch
 * @since 2018-09-11
 */
public class Main {


    public static void main(String[] args)  {


        TestSQL testSQL = ProxyFactory.createProxy(TestSQL.class);


        List<String> strings = testSQL.testDemo(new ArrayList());


    }




}
