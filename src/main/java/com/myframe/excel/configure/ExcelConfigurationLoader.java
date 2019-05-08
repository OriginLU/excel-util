package com.myframe.excel.configure;

import com.myframe.excel.annotation.Excel;
import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.configure.impl.FieldConfigurationLoader;
import com.myframe.excel.configure.impl.MethodConfigurationLoader;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.entity.ExcelConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.util.ReflectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExcelConfigurationLoader {


    private static final List<ConfigurationLoader> EXPORT_REGISTER_LOADER = new ArrayList<>();

    private static final List<ConfigurationLoader> IMPORT_REGISTER_LOADER = new ArrayList<>();

    private static ConcurrentMap<Class, ExcelConfiguration> exportConfCache = new ConcurrentHashMap<>(16);

    private static ConcurrentMap<Class, ExcelConfiguration> importConfCache = new ConcurrentHashMap<>(16);


    static
    {
        EXPORT_REGISTER_LOADER.add(new FieldConfigurationLoader());
        EXPORT_REGISTER_LOADER.add(new MethodConfigurationLoader());

        IMPORT_REGISTER_LOADER.add(new FieldConfigurationLoader());
    }

    public static  ExcelConfiguration getExportConfiguration(Class<?> clazz){

        ExcelConfiguration exportConfiguration = exportConfCache.get(clazz);
        if (exportConfiguration == null)
        {
            exportConfiguration = createExcelConfiguration(clazz);
            exportConfiguration.setConfigurations(loadConfiguration(clazz,EXPORT_REGISTER_LOADER));
            exportConfCache.putIfAbsent(clazz, exportConfiguration);
        }
        return exportConfiguration;

    }

    public static  ExcelConfiguration getImportConfiguration(Class<?> clazz){


        ExcelConfiguration importConfiguration = importConfCache.get(clazz);
        if (importConfiguration == null)
        {
            importConfiguration = createExcelConfiguration(clazz);
            importConfiguration.setConfigurations(loadConfiguration(clazz,IMPORT_REGISTER_LOADER));
            importConfCache.putIfAbsent(clazz, importConfiguration);
        }
        return importConfiguration;

    }




    private static ExcelColumnConfiguration[] loadConfiguration(Class<?> clazz,List<ConfigurationLoader> loaders){

        int arraySize = getArraySize(clazz);

        ExcelColumnConfiguration[] configurations = new ExcelColumnConfiguration[arraySize];
        for (ConfigurationLoader loader : loaders)
        {
            loader.getExcelColumnConfiguration(clazz, configurations);
        }
        return configurations;

    }


    private static int getArraySize(Class<?> clazz){

        return ReflectUtils.getSpecifiedAnnotationFieldsCount(clazz, ExcelColumn.class) + ReflectUtils.getSpecifiedAnnotationMethodsCount(clazz,ExcelColumn.class);
    }


    private static ExcelConfiguration createExcelConfiguration(Class<?> type){


        Excel excel = type.getAnnotation(Excel.class);

        if (null == excel)
        {
            throw new ExcelCreateException("not found excel annotation [@Excel],check please");
        }
        ExcelConfiguration configuration = new ExcelConfiguration();

        configuration.setVersion(excel.version());
        configuration.setExcelName(excel.value());
        configuration.setCreateTitle(excel.isCreateTitle());

        return configuration;
    }

}
