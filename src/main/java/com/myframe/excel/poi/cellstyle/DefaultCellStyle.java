package com.myframe.excel.poi.cellstyle;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public class DefaultCellStyle implements POICellStyle{



    @Override
    public CellStyle getColumnCellStyle(Workbook workbook) {

        Font cellFont = workbook.createFont();
        cellFont.setItalic(false);                                      // 设置字体为斜体字
        cellFont.setFontName("宋体");                                    // 字体应用到当前单元格上
        cellFont.setColor(Font.COLOR_NORMAL);                           // 将字体设置为“红色”
        cellFont.setFontHeightInPoints((short) 10);                      // 将字体大小设置为18px
        cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        CellStyle cellStyle = workbook.createCellStyle();                     //表格样式
        cellStyle.setFont(cellFont);
        cellStyle.setWrapText(true);                                    //设置自动换行
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);              //上边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);             //左边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);            //右边框
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);           //下边框
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        return cellStyle;
    }

    @Override
    public CellStyle getContentCellStyle(Workbook workbook) {

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        cellStyle.setFont(font);

        return cellStyle;
    }

    @Override
    public CellStyle getTitleCellStyle(Workbook workbook) {

        Font ztFont = workbook.createFont();
        ztFont.setFontName("宋体");                                  // 将“宋体”字体应用到当前单元格上
        ztFont.setItalic(false);                                    // 设置字体为斜体字
        ztFont.setStrikeout(false);                                 // 是否添加删除线
        ztFont.setColor(Font.COLOR_NORMAL);                         // 将字体设置为“红色”
        ztFont.setUnderline(Font.U_SINGLE);                         // 添加（Font.U_SINGLE单条下划线/Font.U_DOUBLE双条下划线）
        ztFont.setFontHeightInPoints((short) 16);                   // 将字体大小设置为18px
        ztFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);             // 加粗

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        titleStyle.setFont(ztFont);

        return titleStyle;
    }
}
