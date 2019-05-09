package com.myframe.excel.loader.impl.conf;

import com.myframe.excel.annotation.Excel;
import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.constants.LoadType;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.entity.ExcelConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.exception.RepeatOrderException;
import com.myframe.excel.loader.ColumnConfigurationWrapper;
import com.myframe.excel.loader.ConfigurationLoader;
import com.myframe.excel.loader.impl.wrapper.FieldConfigurationWrapper;
import com.myframe.excel.loader.impl.wrapper.MethodConfigurationWrapper;
import com.myframe.excel.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExcelConfigurationLoader implements ConfigurationLoader {


    private final static ThreadLocal<Map<Class<?>,List<ExcelColumnConfiguration>>> CONFIGURATION_CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private static ConfigurationLoader configurationLoader;

    private ColumnConfigurationWrapper<Field> fieldWrapper;

    private ColumnConfigurationWrapper<Method> methodWrapper;

    private ConcurrentMap<Class, ExcelConfiguration> exportConfCache;

    private ConcurrentMap<Class, ExcelConfiguration> importConfCache;

    private ExcelConfigurationLoader() {

        this.fieldWrapper = new FieldConfigurationWrapper();
        this.methodWrapper = new MethodConfigurationWrapper();
        this.exportConfCache = new ConcurrentHashMap<>(16);
        this.importConfCache = new ConcurrentHashMap<>(16);
    }


    public static ConfigurationLoader getExcelConfigurationLoader(){

        if (null == configurationLoader)
        {
            synchronized (ExcelConfigurationLoader.class)
            {
                if (null == configurationLoader)
                {
                    configurationLoader = new ExcelConfigurationLoader();
                }
            }
        }
        return configurationLoader;
    }


    public ExcelConfiguration getExportConfiguration(Class<?> clazz){

        ExcelConfiguration exportConfiguration = exportConfCache.get(clazz);
        if (null == exportConfiguration)
        {
            exportConfiguration = createExcelConfiguration(clazz,LoadType.EXPORT);
            exportConfCache.putIfAbsent(clazz, exportConfiguration);
        }
        return exportConfiguration;

    }

    public ExcelConfiguration getImportConfiguration(Class<?> clazz){

        ExcelConfiguration importConfiguration = importConfCache.get(clazz);
        if (null == importConfiguration)
        {
            importConfiguration = createExcelConfiguration(clazz,LoadType.IMPORT);
            importConfCache.putIfAbsent(clazz, importConfiguration);
        }
        return importConfiguration;

    }


    private  ExcelConfiguration createExcelConfiguration(Class<?> type, LoadType loadType){


        Excel excel = type.getAnnotation(Excel.class);

        if (null == excel)
        {
            throw new ExcelCreateException("not found excel annotation [@Excel],check please");
        }
        ExcelConfiguration configuration = new ExcelConfiguration();

        configuration.setVersion(excel.version());
        configuration.setExcelName(excel.value());
        configuration.setCreateTitle(excel.isCreateTitle());
        configuration.setConfigurations(getConfiguration(type,loadType));

        return configuration;
    }

    private ExcelColumnConfiguration[] getConfiguration(Class<?> type, LoadType loadType) {


        if (loadType.getType() == LoadType.IMPORT.getType())
        {
            return loadImportConfiguration(type);
        }

        return loadExportConfiguration(type);
    }

    private ExcelColumnConfiguration[] loadImportConfiguration(Class<?> type) {

        try
        {
            loadFieldConfiguration(type);

            return sortConfiguration(type);
        }
        finally
        {
            clear();
        }

    }

    private ExcelColumnConfiguration[] loadExportConfiguration(Class<?> type) {

        try
        {
            loadFieldConfiguration(type);
            loadMethodConfiguration(type);

            return sortConfiguration(type);
        }
        finally
        {
            clear();
        }
    }


    /**
     * sorted column configuration by index(order)
     */
    private ExcelColumnConfiguration[] sortConfiguration(Class<?> type) {

        List<ExcelColumnConfiguration> configurationList = getContext(type);

        int length = configurationList.size();

        ExcelColumnConfiguration[] configurations = new ExcelColumnConfiguration[length];

        Set<Integer> orders = new HashSet<>();
        Deque<Integer> index = new ArrayDeque<>();

        for (int col = 0; col < length; col++)
        {
            ExcelColumnConfiguration columnConf = configurationList.get(col);
            int order = columnConf.getOrder();

            if (order >= length)
            {
                throw new IndexOutOfBoundsException("the specified index [" + order + "] out of bound,max length is " + length);
            }

            if (order > -1)
            {
                if (!orders.add(order))
                {
                    throw new RepeatOrderException("the order must not be repeated, the repeat order is " + order +
                            " in the member [" +columnConf.getColumnName() + "]," + "which same as the" +
                            " member [" + configurationList.get(order).getColumnName() + "]");
                }
            }
            else
            {
                order = (index.size() > 0) ? index.pop() : getFreeIndex(col, configurations);
            }

            if (order != col && configurations[col] == null)
            {
                index.add(col);
            }

            configurations[order] = columnConf;
        }

        return configurations;
    }

    private int getFreeIndex(int index, ExcelColumnConfiguration[] conf) {

        if (index < conf.length - 1 && conf[index] != null)
        {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }


    /**
     * load configuration from field
     */
    private void loadFieldConfiguration(Class<?> type) {

        List<ExcelColumnConfiguration> context = getContext(type);
        ReflectionUtils.doWithFields(type,(field -> {

            if (field.isAnnotationPresent(ExcelColumn.class))
            {
                context.add(fieldWrapper.createExcelColumnConfiguration(field));
            }
        }));
    }


    /**
     * load configuration from method
     */
    private void loadMethodConfiguration(Class<?> type) {


        List<ExcelColumnConfiguration> context = getContext(type);
        ReflectionUtils.doWithMethods(type, method -> {

            if (method.isAnnotationPresent(ExcelColumn.class))
            {
                context.add(methodWrapper.createExcelColumnConfiguration(method));
            }
        });

    }

    private List<ExcelColumnConfiguration> getContext(Class<?> type){

        return CONFIGURATION_CONTEXT.get().computeIfAbsent(type, k -> new ArrayList<>());
    }

    private void clear(){

       CONFIGURATION_CONTEXT.get().clear();
    }



}
