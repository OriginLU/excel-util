package com.myframe.excel.constant;

/**
 * @author lch
 * @since 2018-08-09
 */
public interface VersionConstant {

    /**
     * the version of 2003's excel will be created by HSSFWorkBook,
     * if data size over 65536 that will be thrown {@link java.lang.OutOfMemoryError} Exception
     */
    String      EXCEL_2003          =       "EXCEL_2003";

    /**
     * the version of 2007's excel will be created by XSSFWorkbook,
     * if data size over 65536 that will be thrown {@link java.lang.OutOfMemoryError} Exception
     */
    String      EXCEL_2007          =       "EXCEL_2007";

    /**
     * the version of 2007's excel will be created the excel by SXSSFWorkbook,
     * if data size over 65536 that will be flush data to hard disk and create temp file to storage,
     * and it should be noted that the flushed data not be changed
     */
    String      EXCEL_2007_ADV      =       "EXCEL_2007_ADVANCE";
}
