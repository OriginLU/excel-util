package com.chl.excel.util;

import com.chl.excel.annotation.Excel;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.entity.ExcelCol;
import com.chl.excel.exception.ExcelCreateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * because of poi as the tool of excel for operation is not be safe in the multi thread,
 * you can overwrite the {@link Row#createCell(int)} and {@link Sheet#createRow(int)} method
 * and add lock for guarantee safe operation,but it performance bad than single thread
 *
 * @author LCH
 * @since 2018-06-13
 */
public abstract class POIExcelUtils extends BaseUtils{


    private final static Logger log = LoggerFactory.getLogger(POIExcelUtils.class);

    private static int CELL_WIDTH = 200;


    public static Workbook createExcel(List list, Class type) {

        paramsCheck(list, type);
        ExcelCol[] conf = ExcelConfigureUtil.getExcelColConfiguration(type);
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);
        Sheet sheet = createSheet(workbook, titleName,type);
        int rowIndex = createTitleRow(workbook, sheet, titleName, conf.length);
        rowIndex = createColumnNameRow(workbook, sheet, conf, rowIndex);
        createContentRow(workbook, sheet, list, conf, rowIndex);
        return workbook;
    }



    private static void paramsCheck(List list, Class type) {

        if (type.isAnnotationPresent(Excel.class)) {

        }
    }

    private static Sheet createSheet(Workbook workbook, String titleName,Class type) {

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);
        return sheet;
    }

    /**
     * create cell data for per row
     */
    private static void createContentRow(Workbook workbook, Sheet sheet, List list, ExcelCol[] configs, int rowNum) {


        int length = list.size();
        int columnLength = configs.length;
        CellStyle cellStyle = getCellStyle(workbook);

        for (int rowIndex = rowNum, data = 0; data < length; rowIndex ++, data ++) {
            Row row = sheet.createRow(rowIndex);
            Object obj = list.get(data);
            for (int col = 0; col < columnLength; col++) { //create cell for row

                ExcelCol config = configs[col];
                Object result = getValue(obj,config);
                Cell cell = row.createCell(col);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(convertToString(result, config.getAnnotations()));
            }
        }

    }

    /**
     * create column name for excel
     *
     */
    private static int createColumnNameRow(Workbook workbook, Sheet sheet, ExcelCol[] configs, int rowNum) {

        Row row = sheet.createRow(rowNum);
        CellStyle contentCellStyle = getColumnNameCellStyle(workbook);
        for (int col = 0; col < configs.length; col++) {
            String columnName = getColumnName(configs[col]);
            Cell cell = row.createCell(col);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(columnName);
        }
        rowNum += 1;
        sheet.createFreezePane(0,rowNum,0,rowNum);
        return rowNum;
    }

    /**
     * create title
     */
    private static int createTitleRow(Workbook book, Sheet sheet, String titleName, int columnLength) {


        if (StringUtils.isNotBlank(titleName)) {
            Row titleRow = sheet.createRow(0);
            Cell cell = titleRow.createCell(0);
            cell.setCellValue(titleName);
            cell.setCellStyle(getTitleCellStyle(book));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnLength - 1));
            sheet.createFreezePane(0,1,0,1);
            return 1;
        }
        return 0;
    }


    /**
     * 设置标题样式
     *
     */
    private static CellStyle getTitleCellStyle(Workbook wb) {

        Font ztFont = wb.createFont();
        ztFont.setFontName("宋体");                                  // 将“宋体”字体应用到当前单元格上
        ztFont.setItalic(false);                                    // 设置字体为斜体字
        ztFont.setStrikeout(false);                                 // 是否添加删除线
        ztFont.setColor(Font.COLOR_NORMAL);                         // 将字体设置为“红色”
        ztFont.setUnderline(Font.U_SINGLE);                         // 添加（Font.U_SINGLE单条下划线/Font.U_DOUBLE双条下划线）
        ztFont.setFontHeightInPoints((short) 16);                   // 将字体大小设置为18px
        ztFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);             // 加粗

        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        titleStyle.setFont(ztFont);

        return titleStyle;
    }


    /**
     * 设置单元格样式
     */
    private static CellStyle getColumnNameCellStyle(Workbook wb) {

        Font cellFont = wb.createFont();
        cellFont.setItalic(false);                                      // 设置字体为斜体字
        cellFont.setFontName("宋体");                                    // 字体应用到当前单元格上
        cellFont.setColor(Font.COLOR_NORMAL);                           // 将字体设置为“红色”
        cellFont.setFontHeightInPoints((short) 10);                      // 将字体大小设置为18px
        cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        CellStyle cellStyle = wb.createCellStyle();                     //表格样式
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


    private static CellStyle getCellStyle(Workbook workbook) {


        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        cellStyle.setFont(font);

        return cellStyle;
    }

    /**
     * the excel files is created by multi thread
     */
    public static String createExcelFiles(final List list, final Class type, Integer sheetCount) {

        ExecutorService executorService = ExecutorFactory.getInstance();
        String name = getName(type);
        int cycleCount = getCycleCount(list.size(),sheetCount);
        String sysPath = getSystemPath(name);
        String prefix = sysPath + name + "_";
        for (int i = 0; i < cycleCount; i++) {
            final String path = prefix + i + ".xls";
            final List nextList = getNextList(list,i,sheetCount);
            executorService.execute(createRunnable(nextList, type,path));
        }
        executorService.shutdown();
        return sysPath;
    }



    private static String getSystemPath(String temp) {

        String path = "d:/excel/";
//        String path = System.getProperty("java.io.tmpdir");
        path = (path.lastIndexOf('/') == path.length() - 1) ? (path + temp) : (path + File.separator + temp);
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        return file.getAbsolutePath() + File.separator;
    }

    private static Runnable createRunnable(final List list, final Class type,final String path){

        return new Runnable() {
            @Override
            public void run() {
                try {
                    Workbook excel = createExcel(list, type);
                    FileOutputStream outputStream = new FileOutputStream(path);
                    excel.write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                }catch (Exception e){
                    throw new ExcelCreateException("create excel error : ", e);
                }
            }
        };
    }

    private static Callable<Workbook> createCallable(final List list, final Class type) {

        return new Callable<Workbook>() {
            @Override
            public Workbook call() {
                return createExcel(list, type);
            }
        };
    }



    private static Workbook getWorkBook(List<Future<Workbook>> futures, Class type) {

        try {
            String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
            String excelTitleName = ExcelConfigureUtil.getExcelTitleName(type);
            Workbook toWorkBook = WorkBookFactory.createWorkBook(excelVersion);
            for (int i = 0; i < futures.size(); i++) {
                Workbook fromWorkBook = futures.get(i).get();
                mergeExcel(fromWorkBook, toWorkBook, excelTitleName + "_" + i);
            }
            return toWorkBook;
        } catch (Exception e) {
            log.error("create excel error : ", e);
            throw new ExcelCreateException("create excel error : ", e);
        } finally {
            ExecutorFactory.close();
        }
    }

    /**
     * merge excel
     */
    private static void mergeExcel(Workbook fromWorkBook, Workbook toWorkBook, String sheetName) {

        try {
            for (int i = 0; i < fromWorkBook.getNumberOfSheets(); i++) {
                Sheet oldSheet = fromWorkBook.getSheetAt(i);
                Sheet newSheet = toWorkBook.createSheet(sheetName);
                copySheet(toWorkBook, oldSheet, newSheet);
            }
        } catch (Exception e) {
            throw new ExcelCreateException("create excel error", e);
        }
    }

    private class XSSFDateUtil extends DateUtil {

    }


    private static void copySheet(Workbook wb, Sheet fromSheet, Sheet toSheet) {

        mergeSheetAllRegion(fromSheet, toSheet);
        for (int i = 0; i <= fromSheet.getRow(fromSheet.getFirstRowNum()).getLastCellNum(); i++)
        {
            toSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
        }
        for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext(); )
        {
            Row oldRow = (Row) rowIt.next();
            Row newRow = toSheet.createRow(oldRow.getRowNum());
            copyRow(wb, oldRow, newRow);
        }
    }

    private static void mergeSheetAllRegion(Sheet fromSheet, Sheet toSheet) {//合并单元格


        int num = fromSheet.getNumMergedRegions();
        for (int i = 0; i < num; i++)
        {
            CellRangeAddress cellR = fromSheet.getMergedRegion(i);
            toSheet.addMergedRegion(cellR);
        }
    }

    private static void copyCell(Workbook wb, Cell fromCell, Cell toCell) {

        CellStyle newStyle = wb.createCellStyle();
        copyCellStyle(fromCell.getCellStyle(), newStyle);
        toCell.setCellStyle(newStyle);

        if (fromCell.getCellComment() != null)
        {
            toCell.setCellComment(fromCell.getCellComment());
        }
        int fromCellType = fromCell.getCellType();
        toCell.setCellType(fromCellType);

        if (fromCellType == XSSFCell.CELL_TYPE_NUMERIC)
        {
            if (XSSFDateUtil.isCellDateFormatted(fromCell))
            {
                toCell.setCellValue(fromCell.getDateCellValue());
            }
            else
            {
                toCell.setCellValue(fromCell.getNumericCellValue());
            }
        }
        else if (fromCellType == XSSFCell.CELL_TYPE_STRING)
        {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        }
        else if (fromCellType == XSSFCell.CELL_TYPE_BLANK)
        {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        }
        else if (fromCellType == XSSFCell.CELL_TYPE_BOOLEAN)
        {
            toCell.setCellValue(fromCell.getBooleanCellValue());
        }
        else if (fromCellType == XSSFCell.CELL_TYPE_ERROR)
        {
            toCell.setCellErrorValue(fromCell.getErrorCellValue());
        }
        else if (fromCellType == XSSFCell.CELL_TYPE_FORMULA)
        {
            toCell.setCellFormula(fromCell.getCellFormula());
        }
        else
        {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        }
    }


    private static void copyRow(Workbook wb, Row oldRow, Row toRow) {

        toRow.setHeight(oldRow.getHeight());
        for (Iterator cellIt = oldRow.cellIterator(); cellIt.hasNext(); )
        {
            Cell tmpCell = (XSSFCell) cellIt.next();
            Cell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, tmpCell, newCell);
        }
    }

    private static void copyCellStyle(CellStyle fromStyle, CellStyle toStyle) {

        toStyle.cloneStyleFrom(fromStyle);//此一行代码搞定
    }


}
