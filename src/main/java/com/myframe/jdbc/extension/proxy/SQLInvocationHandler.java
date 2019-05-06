package com.myframe.jdbc.extension.proxy;

import com.myframe.jdbc.extension.conf.SQLConfig;
import com.myframe.jdbc.extension.context.SQLContext;
import com.myframe.jdbc.extension.util.EnvUtils;
import com.myframe.jdbc.extension.util.KeyNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

class SQLInvocationHandler implements InvocationHandler {


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        log.info("invoke method ["  + method.getDeclaringClass() + "#" + method.getName() + "]");
        String name = KeyNameUtils.getName(method);
        Map<String, Object> env = EnvUtils.getEnv(args);
        SQLContext context = SQLContext.getInstance();
        SQLConfig sqlConfig = context.getSQLConfig(name,method.getDeclaringClass());

        String sql = sqlConfig.getSQL(env);

        log.info("SQL[" + sql + "]");


        return null;
    }
}
