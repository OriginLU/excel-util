package com.chl.excel.configure;

import com.chl.excel.entity.ExcelColumnConfiguration;

public interface ConfigurationLoader {


    ExcelColumnConfiguration[] getExcelColumnConfiguration(Class<?> clazz, ExcelColumnConfiguration[] conf);
}
