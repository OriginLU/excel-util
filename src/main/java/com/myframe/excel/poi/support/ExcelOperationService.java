package com.myframe.excel.poi.support;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;

public interface ExcelOperationService{



    Workbook exportSheet(List<?> data, Class<?> type);

    Workbook exportMultiSheet(List<?> data, Class<?> type,int maxRowNum);

    List<?> importData(InputStream ins,Class<?> type);

    void merge(Workbook fromWorkBook, Workbook toWorkBook, String sheetName);

}
