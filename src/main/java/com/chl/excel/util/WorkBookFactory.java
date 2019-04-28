package com.chl.excel.util;

import com.chl.excel.constant.VersionConstant;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author lch
 * @since 2018-08-09
 */
public abstract class WorkBookFactory {

    public static Workbook createWorkBook(String version){

        switch (version){

            case VersionConstant.EXCEL_2003:
                return new HSSFWorkbook();
            case VersionConstant.EXCEL_2007:
            case VersionConstant.EXCEL_2007_ADV:
                default:
                    return new XSSFWorkbook();
        }
    }
}
