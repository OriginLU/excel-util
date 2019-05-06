package com.myframe.excel.configure;

import com.myframe.excel.entity.ExcelColumnConfiguration;

public interface ConfigurationLoader {


    ExcelColumnConfiguration[] getExcelColumnConfiguration(Class<?> clazz, ExcelColumnConfiguration[] conf);
}
