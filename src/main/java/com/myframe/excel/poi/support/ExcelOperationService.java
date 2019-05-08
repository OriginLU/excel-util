package com.myframe.excel.poi.support;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;

public interface ExcelOperationService {



    Workbook exportSingleSheet(List<?> data, Class<?> type, boolean isCreateTitle);

    Workbook exportMultiSheet(List<?> data, Class<?> type,int maxRowNum,boolean isCreateTitle,boolean isParallelThread);

    List<?> importData(InputStream ins,Class<?> type);


    void merge(Workbook fromWorkBook, Workbook toWorkBook, String sheetName);

}
