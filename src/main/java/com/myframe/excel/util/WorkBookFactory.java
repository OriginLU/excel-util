package com.myframe.excel.util;

import com.myframe.excel.constant.VersionConstant;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author lch
 * @since 2018-08-09
 */
public abstract class WorkBookFactory {

    public static Workbook createWorkBook(String version){

        switch (version){

            case VersionConstant.EXCEL_2007:
                return new XSSFWorkbook();

            case VersionConstant.EXCEL_2007_ADV:
                return new SXSSFWorkbook(-1);

            case VersionConstant.EXCEL_2003:
            default:
                return new HSSFWorkbook();
        }
    }
}
