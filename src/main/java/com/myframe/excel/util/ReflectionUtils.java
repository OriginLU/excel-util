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


    public static Object invokeMethod(Object target, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException {

        Class<?> clazz = target.getClass();
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
        {
            classes[i] = args[i].getClass();
        }
        Method method = getMethod(clazz, methodName, classes);

        return invokeMethod(target,method,args);
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


    /**
     * 获取目标方法
     */
    private static Method getMethod(Class<?> target, String methodName, Class<?> ... paramTypes) throws NoSuchMethodException {

        try
        {
            return target.getDeclaredMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException e)
        {
            target = target.getSuperclass();
            if (target == null)
            {
                throw new NoSuchMethodException("not found [" + methodName + "(" + Arrays.asList(paramTypes) + ")] method");
            }
            return getMethod(target, methodName, paramTypes);
        }
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

    public static void doWithFields(Class<?> clazz, FieldCallBack callBack) {

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            doWithFields(superclass, callBack);
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            try {
                callBack.doWith(field);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Not allowed to access field '" + field.getName() + "':" + e);
            }
        }
    }


    public static void doWithMethods(Class<?> clazz, MethodCallback callBack) {

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            doWithMethods(superclass, callBack);
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods)
        {
            try
            {
                callBack.doWith(method);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "':" + e);
            }
        }
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
