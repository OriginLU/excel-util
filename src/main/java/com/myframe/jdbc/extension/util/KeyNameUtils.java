package com.myframe.jdbc.extension.util;

import java.lang.reflect.Method;

public abstract class KeyNameUtils {


    public static String getName(Method method){

        StringBuilder name = new StringBuilder();

        name.append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append("(");

        Class<?>[] parameterTypes = method.getParameterTypes();

        if (!CollectionUtils.isEmpty(parameterTypes))
        {

            StringBuilder parameters = new StringBuilder();
            for (Class<?> parameterType : parameterTypes)
            {

                parameters.append(parameterType.getSimpleName()).append(",");
            }
            name.append(parameters.substring(0,parameters.length() - 1));

        }
        name.append(")");
        return name.toString();
    }
}
