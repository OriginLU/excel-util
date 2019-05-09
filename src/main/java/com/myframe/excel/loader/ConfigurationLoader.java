package com.myframe.excel.loader;

import com.myframe.excel.entity.ExcelConfiguration;

public interface ConfigurationLoader {

    /**
     * load configuration for excel import
     */
    ExcelConfiguration getImportConfiguration(Class<?> clazz);

    /**
     * load configuration for excel export
     */
    ExcelConfiguration getExportConfiguration(Class<?> clazz);
}
