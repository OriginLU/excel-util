package com.myframe.excel.poi.support;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;

public interface ExcelOperationService {



    Workbook export(List<?> data, Class<?> type,boolean isCreateTitle);


    List<?> importData(InputStream ins,Class<?> type);


    void mergeExcel(Workbook fromWorkBook, Workbook toWorkBook, String sheetName);

}
