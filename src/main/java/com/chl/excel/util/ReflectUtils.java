package com.chl.excel.util;

import java.lang.annotation.Annotation;
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
public abstract class ReflectUtils {


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

    public static void setFieldValue(String fieldName, Object obj, Object value) throws NoSuchFieldException {

        final Class<?> clazz = obj.getClass();
        Field field = getField(clazz, fieldName);
        setFieldValue(field, obj, value);

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
     * 获取指定域
     */
    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {

        try
        {
            return clazz.getDeclaredField(name);
        }
        catch (NoSuchFieldException e)
        {
            Class superclass = clazz.getSuperclass();
            if (superclass == null)
            {
                throw new NoSuchFieldException("not found field [" + name + "]");
            }
            return getField(superclass, name);
        }
    }

    /**
     * 获取目标方法
     */
    public static Method getMethod(Class<?> target, String methodName, Class<?> ... paramTypes) throws NoSuchMethodException {

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

    /**
     * 获取的指定注解声明域
     */
    public static List<Field> getSpecifiedAnnotationFields(Class clazz, Class<? extends Annotation> annotationClass) {

        List<Field> list;
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            list = getSpecifiedAnnotationFields(superclass, annotationClass);
        }
        else
        {
            list = new ArrayList<>();
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(annotationClass))
            {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 获取的指定注解方法
     */
    public static List<Method> getSpecifiedAnnotationMethods(Class clazz, Class<? extends Annotation> annotationClass) {

        List<Method> list;
        Class superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass)
        {
            list = getSpecifiedAnnotationMethods(superclass, annotationClass);
        }
        else
        {
            list = new ArrayList<>();
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(annotationClass))
            {
                list.add(method);
            }
        }
        return list;
    }



}
