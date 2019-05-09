package com.myframe.excel.loader;

import com.myframe.excel.entity.ExcelColumnConfiguration;

import java.lang.reflect.Member;

public interface ColumnConfigurationWrapper<T extends Member> {


    ExcelColumnConfiguration createExcelColumnConfiguration(T member);

}
