package com.chl.excel.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author LCH
 * @since 2017-08-29 22:03:24
 */
public abstract class ReflectUtils {


    public static void setFieldValue(Field field, Object obj, Object value) {

        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            //ignore this exception
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void setFieldValue(String fieldName, Object obj, Object value) throws NoSuchFieldException {

        final Class clazz = obj.getClass();
        Field field = getField(clazz, fieldName);
        setFieldValue(field, obj, value);

    }


    /**
     * @param target
     * @param methodName
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException {

        Class clazz = target.getClass();
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        Method method = getMethod(clazz, methodName, classes);
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            //ignore this exception
        } finally {
            method.setAccessible(accessible);
        }
        return null;
    }

    /**
     * @param target
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object target, Method method, Object... args) throws InvocationTargetException {

        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            //ignore this exception
        } finally {
            method.setAccessible(accessible);
        }
        return null;
    }


    /**
     * 获取指定域
     *
     * @param clazz 搜索字段指定类
     * @param name  搜字段名
     * @return 指定字段类型
     * @throws NoSuchFieldException 未找到指定字段类型
     */
    public static Field getField(Class clazz, String name) throws NoSuchFieldException {

        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class superclass = clazz.getSuperclass();
            if (superclass == null) {
                throw new NoSuchFieldException("not found field [" + name + "]");
            }
            field = getField(superclass, name);
        }
        return field;
    }

    /**
     * 获取目标方法
     *
     * @param target     目标类
     * @param methodName 目标方法
     * @param paramTypes 参数列表
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class target, String methodName, Class... paramTypes) throws NoSuchMethodException {

        try {
            return target.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            target = target.getSuperclass();
            if (target == null) {
                throw new NoSuchMethodException("not found [" + methodName + "(" + Arrays.asList(paramTypes) + ")] method");
            }
            return getMethod(target, methodName, paramTypes);
        }
    }


    /**
     * @param obj
     * @param name
     * @param <T>
     * @return
     * @throws NoSuchFieldException
     */
    public static <T> T getFieldValue(Object obj, String name) throws NoSuchFieldException {

        final Class clazz = obj.getClass();
        Field field = getField(clazz, name);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            //ignore this exception
        } finally {
            field.setAccessible(accessible);
        }
        return null;
    }

    /**
     * @param obj
     * @param field
     * @param <T>
     * @return
     * @throws NoSuchFieldException
     */
    public static <T> T getFieldValue(Object obj, Field field){

        final Class clazz = obj.getClass();
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            //ignore this exception
        } finally {
            field.setAccessible(accessible);
        }
        return null;
    }

    /**
     * 获取所有的声明域，包含父类
     *
     * @param clazz
     * @return
     */
    public static List<Field> getFields(Class clazz) {

        List fields;
        Class superclass = clazz.getSuperclass();
        if (Object.class != superclass) {
            fields = getFields(superclass);
        }
        else {
            fields = new ArrayList();
        }
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }

    /**
     * 获取所有的声明方法，包含父类
     *
     * @param clazz
     * @return
     */
    public static List<Method> getMethods(Class clazz) {

        List methods;
        Class superclass = clazz.getSuperclass();
        if (superclass != null && Object.class != superclass) {
            methods = getMethods(superclass);
        }
        else {
            methods = new ArrayList();
        }
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    /**
     * 获取的指定注解声明域
     *
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static List<Field> getSpecifiedAnnotationFields(Class clazz, Class annotationClass) {

        List list;
        Class superclass = clazz.getSuperclass();
        if (Object.class != superclass) {
            list = getSpecifiedAnnotationFields(superclass, annotationClass);
        }else {
            list = new ArrayList();
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClass)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 获取的指定注解方法
     *
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static List<Method> getSpecifiedAnnotationMethods(Class clazz, Class annotationClass) {

        List list;
        Class superclass = clazz.getSuperclass();
        if (Object.class != superclass) {
            list = getSpecifiedAnnotationMethods(superclass, annotationClass);
        }else {
            list = new ArrayList();
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass)) {
                list.add(method);
            }
        }
        return list;
    }


    public static Annotation[] getMemberAnnotations(Member member){

        if (member instanceof Field){
            return ((Field) member).getAnnotations();
        }
        else if (member instanceof Method){
            return ((Method) member).getAnnotations();
        }
        else {
            throw new IllegalArgumentException("input type has error, not support [" + member.getName() + "],check please");
        }
    }


    public static <T extends Annotation> T getMemberAnnotation(Member member,Class type){

        if (member instanceof Field){
            return (T) ((Field) member).getAnnotation(type);
        }
        else if (member instanceof Method){
            return (T) ((Method) member).getAnnotation(type);
        }
        else {
            throw new IllegalArgumentException("input type has error, not support [" + member.getName() + "],check please");
        }
    }


    public static Object getMemberValue(Object src, Member member,Object ...args) throws InvocationTargetException {

        if (member instanceof Field){
            return getFieldValue(src, (Field) member);
        }
        else if (member instanceof Method){
            return invokeMethod(src, (Method) member,args);
        }
        else {
            throw new IllegalArgumentException("input type has error, not support [" + member.getName() + "],check please");
        }
    }

}
