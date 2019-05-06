package com.myframe.jdbc.extension.proxy;

import java.lang.reflect.Proxy;

public class ProxyFactory {



    public static <T> T createProxy(Class<?> interfaceClass){

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class[]{interfaceClass},new SQLInvocationHandler());
    }
}
