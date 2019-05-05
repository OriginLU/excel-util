package com.chl.excel.configure;

import com.chl.excel.annotation.Excel;
import com.chl.excel.configure.impl.FieldConfigurationLoader;
import com.chl.excel.configure.impl.MethodConfigurationLoader;
import com.chl.excel.entity.ExcelColumnConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExcelConfigurationLoader {


    private static final List<ConfigurationLoader> REGISTER_LOADER = new ArrayList<>();

    private static ConcurrentMap<Class, ExcelColumnConfiguration[]> excelConf = new ConcurrentHashMap<>(16);


    static
    {
        REGISTER_LOADER.add(new FieldConfigurationLoader());
        REGISTER_LOADER.add(new MethodConfigurationLoader());
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


        ExcelColumnConfiguration[] configurations = null;
        for (ConfigurationLoader loader : REGISTER_LOADER)
        {
            configurations = loader.getExcelColumnConfiguration(clazz, configurations);
        }
        return configurations;

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
