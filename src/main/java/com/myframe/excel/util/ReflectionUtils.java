package com.myframe.excel.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LCH
 * @since 2017-08-29 22:03:24
 */
public abstract class ReflectionUtils {


    public static void setFieldValue(Field field, Object obj, Object value) {

        boolean accessible = field.isAccessible();
        if (!accessible)
        {
            field.setAccessible(true);
        }
        try
        {
            field.set(obj, value);
        }
        catch (IllegalAccessException e)
        {
            //ignore this exception
        }
    }


    public static Object invokeMethod(Object target, Method method, Object... args) throws InvocationTargetException {

        boolean accessible = method.isAccessible();
        if (!accessible)
        {
            method.setAccessible(true);
        }
        try
        {
            return method.invoke(target, args);
        }
        catch (IllegalAccessException e)
        {
            //ignore this exception
        }
        return null;
    }



    public static Object getFieldValue(Object obj, Field field){

        boolean accessible = field.isAccessible();
        if (!accessible)
        {
            field.setAccessible(true);
        }
        try
        {
            return field.get(obj);
        }
        catch (IllegalAccessException e)
        {
            //ignore this exception
        }
        return null;
    }

    /**
     * 获取所有的声明域，包含父类
     */
    public static List<Field> getFields(Class<?> clazz) {

        List<Field> fields;
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            fields = getFields(superclass);
        }
        else
        {
            fields = new ArrayList<>();
        }
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }

    /**
     * 获取所有的声明方法，包含父类
     */
    public static List<Method> getMethods(Class clazz) {

        List<Method> methods;
        Class superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            methods = getMethods(superclass);
        }
        else
        {
            methods = new ArrayList<>();
        }
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    public static void doWithFields(Class<?> targetClass, FieldCallBack fc) {


        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields)
            {
                try
                {
                    fc.doWith(field);
                }
                catch (IllegalAccessException ex)
                {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);
    }


    public static void doWithMethods(Class<?> targetClass, MethodCallback mc) {

        do {
            Method[] methods = targetClass.getDeclaredMethods();
            for (Method method : methods)
            {
                try
                {
                    mc.doWith(method);
                }
                catch (IllegalAccessException ex)
                {
                    throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);
    }


    public interface FieldCallBack{

        /**
         * Perform an operation using the given field.
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }


    public interface MethodCallback{

        /**
         * Perform an operation using the given method.
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }




}
