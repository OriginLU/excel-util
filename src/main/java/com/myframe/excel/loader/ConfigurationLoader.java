package com.myframe.excel.loader;

import com.myframe.excel.entity.ExcelColumnConfiguration;

public interface ConfigurationLoader {


    ExcelColumnConfiguration[] getExcelColumnConfiguration(Class<?> clazz, ExcelColumnConfiguration[] conf);
}
