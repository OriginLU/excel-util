package com.myframe.excel.poi.cellstyle;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

public interface POICellStyle {


    CellStyle getTitleCellStyle(Workbook workbook);

    CellStyle getColumnCellStyle(Workbook workbook);

    CellStyle getContentCellStyle(Workbook workbook);
}
