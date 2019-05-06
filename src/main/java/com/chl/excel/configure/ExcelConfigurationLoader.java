package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.impl.FieldConfigurationLoader;
import com.chl.excel.configure.impl.MethodConfigurationLoader;
import com.chl.excel.entity.ExcelColumnConfiguration;
import com.chl.excel.util.ReflectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExcelConfigurationLoader {


    private static final List<ConfigurationLoader> EXPORT_REGISTER_LOADER = new ArrayList<>();

    private static final List<ConfigurationLoader> IMPORT_REGISTER_LOADER = new ArrayList<>();

    private static ConcurrentMap<Class, ExcelColumnConfiguration[]> excelConf = new ConcurrentHashMap<>(16);


    static
    {
        EXPORT_REGISTER_LOADER.add(new FieldConfigurationLoader());
        EXPORT_REGISTER_LOADER.add(new MethodConfigurationLoader());

        IMPORT_REGISTER_LOADER.add(new FieldConfigurationLoader());
    }

    public static  ExcelColumnConfiguration[] getConfiguration(Class<?> clazz){


        ExcelColumnConfiguration[] conf = excelConf.get(clazz);
        if (conf == null)
        {
            conf = loadConfiguration(clazz);
            excelConf.putIfAbsent(clazz, conf);
        }
        return conf;

    }


    private static ExcelColumnConfiguration[] loadConfiguration(Class<?> clazz){

        int arraySize = getArraySize(clazz);

        ExcelColumnConfiguration[] configurations = new ExcelColumnConfiguration[arraySize];
        for (ConfigurationLoader loader : EXPORT_REGISTER_LOADER)
        {
            loader.getExcelColumnConfiguration(clazz, configurations);
        }
        return configurations;

    }


    private static int getArraySize(Class<?> clazz){

        return ReflectUtils.getSpecifiedAnnotationFieldsCount(clazz, ExcelColumn.class) + ReflectUtils.getSpecifiedAnnotationMethodsCount(clazz,ExcelColumn.class);
    }


    public static String getExcelTitleName(Class<?> type) {

        Excel annotation = type.getAnnotation(Excel.class);
        return annotation.value();
    }

    public static String getExcelVersion(Class<?> type) {

        Excel annotation = type.getAnnotation(Excel.class);
        return annotation.version();
    }
}
