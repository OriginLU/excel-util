package com.chl.excel.util;

import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.ExcelCreateException;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author lch
 * @since 2018-08-16
 */
 abstract class BaseUtils {


    protected static int SHEET_COUNT = 1000;

    private static Sequence sequence = new Sequence(2l, 2l);


    protected static Object getResult(ExcelColumnConf config, Object obj) {

        try {
            Field field = config.getAnnotationField();
            if (field != null) {
                return ReflectUtils.getFieldValue(obj, field);
            }
            Method method = config.getAnnotationMethod();
            if (method != null) {
                return ReflectUtils.invokeMethod(obj, method);
            }
        } catch (InvocationTargetException e) {
            throw new ExcelCreateException("create excel error ", e);
        }
        return null;
    }

    protected static String convertToString(Object result, Map<Class, Annotation> ans) {
        if (result == null) {
            return "";
        }
        return result.toString();
    }

    protected static String getColumnName(ExcelColumnConf conf) {

        Map<Class, Annotation> annotations = conf.getAnnotations();
        ExcelColumn excelColumn = (ExcelColumn) annotations.get(ExcelColumn.class);
        String columnName = excelColumn.columnTitle();
        if (StringUtils.isBlank(columnName)) {
            Field field = conf.getAnnotationField();
            return field.getName();
        }
        return columnName;
    }

    protected static List getNextList(List list, int index, Integer sheetCnt) {

        int sheetCount = sheetCnt == null ? SHEET_COUNT : sheetCnt;
        int length = list.size();                           // collection size
        int startIndex = index * sheetCount;
        int endIndex = startIndex + sheetCount;
        endIndex = length > endIndex ? endIndex : length;
        return list.subList(startIndex, endIndex);
    }

    protected static int getCycleCount(int size,Integer sheetCnt) {

        int sheetCount = sheetCnt == null ? SHEET_COUNT : sheetCnt;
        int cycleCount = 0;
        if ((size % sheetCount) != 0) {
            return (size / sheetCount) + 1;
        }
        return (size / sheetCount);
    }

    protected static String getName(Class type){

        String temp = sequence.nextId().toString();
        String name = ExcelConfigureUtil.getExcelTitleName(type);
        if (StringUtils.isBlank(name)){
            name = type.getSimpleName();
        }
        return name + "_" + temp;
    }


    /**
     * create executor service by ExecutorFactory
     */
    protected static class ExecutorFactory {

        private static ExecutorService executorService;

        private static int coreCount = Runtime.getRuntime().availableProcessors();

        public static synchronized ExecutorService getInstance() {

            if (executorService != null && !executorService.isShutdown()) {
                return executorService;
            }
            executorService = Executors.newFixedThreadPool(coreCount, new ExecutorThreadFactory());
            return executorService;
        }

        public static synchronized void close() {

            if (executorService != null && executorService.isShutdown()) {
                executorService.shutdown();
            }
        }

        /**
         * when you use the {@link ExecutorService#execute(Runnable)}
         * throw exception will be catch by the handler for exception
         */
        private static class ExecutorExceptionHandler implements Thread.UncaughtExceptionHandler {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        }

        /**
         * create thread factory for exception
         */
        private static class ExecutorThreadFactory implements ThreadFactory {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(new ExecutorExceptionHandler());
                return t;
            }
        }
    }

}
