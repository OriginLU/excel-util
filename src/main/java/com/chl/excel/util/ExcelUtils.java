package com.chl.excel.util;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.ExcelCreateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private final static Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private static int CELL_WIDTH = 20;

    private static int SHEET_COUNT = 300;

    private static Sequence sequence = new Sequence(1l, 1l);


    public static Workbook createExcel(List list, Class type) {
        log.info(Thread.currentThread().getName() + " : invoke create excel");
        return createExcel(list, type, true, true);
    }

    public static Workbook createExcel(List list, Class type, boolean isCreateTitle, boolean isCreateColumnName) {

        log.info("===============create excel===============");
        if (type.getAnnotation(Excel.class) == null) {

        }
        int rowNum = 0;
        int length = list.size();
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);

        Sheet sheet = workbook.createSheet(titleName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);
        rowNum = createTitleRow(workbook, sheet, titleName, conf.length, isCreateTitle);
        rowNum = createColumnName(workbook, sheet, conf, rowNum, isCreateColumnName);

        for (int i = rowNum, j = 0; j < length; i++, j++) {
            createDataRow(sheet.createRow(i), list.get(j), conf);
        }
        return workbook;
    }

    private static int createColumnName(Workbook workbook, Sheet sheet, ExcelColumnConf[] conf, int rowNum, boolean isCreateColumnName) {

        if (isCreateColumnName) {
            Row row = sheet.createRow(rowNum);
            for (int i = 0; i < conf.length; i++) {
                Map<Class, Annotation> annotations = conf[i].getAnnotations();
                ExcelColumn excelColumn = (ExcelColumn) annotations.get(ExcelColumn.class);
                String columnName = excelColumn.columnTitle();
                row.createCell(i).setCellValue(columnName);

            }
            return (rowNum + 1);
        }
        return rowNum;
    }

    private static int createTitleRow(Workbook book, Sheet sheet, String titleName, int columnLength, boolean isCreateTitle) {

        if (isCreateTitle) {
            if (StringUtils.isNotBlank(titleName)) {
                Row titleRow = sheet.createRow(0);
                Cell cell = titleRow.createCell(0);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnLength - 1));
                cell.setCellStyle(getTitleCellStyle(book));
                cell.setCellValue(titleName);
                return 1;
            }
        }
        return 0;
    }


    /**
     * 设置标题样式
     *
     * @param wb
     * @return
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
     * create executor service by ExecutorFactory
     */
    private static class ExecutorFactory {

        private static ExecutorService executorService;

        private static int coreCount = Runtime.getRuntime().availableProcessors();

        public static synchronized ExecutorService getInstance() {

            if (executorService != null && !executorService.isShutdown()) {
                return executorService;
            }
            executorService = Executors.newFixedThreadPool(coreCount, new ExecutorThreadFactory());
            return executorService;
        }

        public static synchronized void close() {

            if (executorService != null && executorService.isShutdown()) {
                executorService.shutdown();
            }
        }

        /**
         * when you use the {@link ExecutorService#execute(Runnable)}
         * throw exception will be catch by the handler for exception
         */
        private static class ExecutorExceptionHandler implements Thread.UncaughtExceptionHandler {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(t.getName() + " : ", e);
            }
        }

        /**
         * create thread factory for exception
         */
        private static class ExecutorThreadFactory implements ThreadFactory {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(new ExecutorExceptionHandler());
                return t;
            }
        }
    }


    public static void close() {
        ExecutorFactory.close();
    }

    public static Workbook createExcelAdvance(final List list, final Class type) {

        List<Future<Workbook>> futures = new ArrayList();
        ExecutorService executorService = ExecutorFactory.getInstance();
        String temp = sequence.nextId().toString();             // create temp file name by sequence,it should be only one
        int cycleCount = getCycleCount(list.size());

        for (int i = 0; i < cycleCount; i++) {
            final List nextList = getNextList(list, i);
            Future future = executorService.submit(createCallableTask(nextList, type));
            futures.add(future);
        }
        return getWorkBook(futures, type);

    }



    private static Callable<Workbook> createCallableTask(final List list, final Class type) {

        return new Callable<Workbook>() {
            @Override
            public Workbook call() {
                return createExcel(list, type);
            }
        };
    }


    private static Callable<String> createCallableTask(final List list, final Class type, final String name) {

        return new Callable<String>() {
            @Override
            public String call() {
                try {
                    Workbook excel = createExcel(list, type);
                    FileOutputStream fos = new FileOutputStream(name);
                    excel.write(fos);
                    fos.flush();
                    fos.close();
                    return name;
                } catch (IOException e) {
                    log.error("create file error : ", e);
                    throw new ExcelCreateException("create file error : ", e);
                }
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
        }
    }

    private static List getNextList(List list, int index) {

        int length = list.size();                           // collection size
        int startIndex = index * SHEET_COUNT;
        int endIndex = startIndex + SHEET_COUNT;
        endIndex = length > endIndex ? endIndex : length;
        return list.subList(startIndex, endIndex);
    }

    private static int getCycleCount(int size) {

        int cycleCount = 0;
        if ((size % SHEET_COUNT) != 0) {
            return (size / SHEET_COUNT) + 1;
        }
        return (size / SHEET_COUNT);
    }


    /**
     * 设置单元格样式
     *
     * @param wb
     * @return
     */
    private static CellStyle getContentCellStyle(Workbook wb) {

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

    private static void createDataRow(Row row, Object obj, ExcelColumnConf[] configs) {

        try {

            Object result = null;
            for (int i = 0, length = configs.length; i < length; i++) {
                ExcelColumnConf config = configs[i];
                Field field = config.getAnnotationField();
                Method method = config.getAnnotationMethod();
                if (field != null) {
                    result = ReflectUtils.getFieldValue(obj, field);
                }
                if (method != null) {
                    result = ReflectUtils.invokeMethod(obj, method);
                }
                row.createCell(i).setCellValue(convertToString(result, config.getAnnotations()));
            }
        } catch (InvocationTargetException e) {
            log.error("invoke method throw exception : ", e);
            throw new ExcelCreateException("invoke method error ", e);
        }
    }

    private static String convertToString(Object result, Map<Class, Annotation> ans) {
        if (result == null) {
            return "";
        }
        return result.toString();
    }


    private static CellStyle getCellStyle(SXSSFWorkbook workbook) {


        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        cellStyle.setFont(font);

        return cellStyle;
    }


    private static void mergeExcel(Workbook toWorkBook, String path) {

        try {
            File file = new File(path);
            if (file.exists()) {
                String name = file.getName();
                name = name.substring(0, name.indexOf("."));
                InputStream in = new FileInputStream(path);
                XSSFWorkbook fromWorkBook = new XSSFWorkbook(in);
                for (int i = 0; i < fromWorkBook.getNumberOfSheets(); i++) {//遍历每个sheet
                    Sheet oldSheet = fromWorkBook.getSheetAt(i);
                    Sheet newSheet = toWorkBook.createSheet(name);
                    copySheet(toWorkBook, oldSheet, newSheet);
                }
                file.delete();
            }
        } catch (Exception e) {
            throw new ExcelCreateException("create excel error", e);
        }
    }

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
        for (int i = 0; i <= fromSheet.getRow(fromSheet.getFirstRowNum()).getLastCellNum(); i++) {
            toSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
        }
        for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext(); ) {
            Row oldRow = (Row) rowIt.next();
            Row newRow = toSheet.createRow(oldRow.getRowNum());
            copyRow(wb, oldRow, newRow);
        }
    }

    private static void mergeSheetAllRegion(Sheet fromSheet, Sheet toSheet) {//合并单元格
        int num = fromSheet.getNumMergedRegions();
        CellRangeAddress cellR = null;
        for (int i = 0; i < num; i++) {
            cellR = fromSheet.getMergedRegion(i);
            toSheet.addMergedRegion(cellR);
        }
    }

    private static void copyCell(Workbook wb, Cell fromCell, Cell toCell) {
        CellStyle newStyle = wb.createCellStyle();
        copyCellStyle(fromCell.getCellStyle(), newStyle);
        toCell.setCellStyle(newStyle);
        if (fromCell.getCellComment() != null) {
            toCell.setCellComment(fromCell.getCellComment());
        }
        int fromCellType = fromCell.getCellType();
        toCell.setCellType(fromCellType);
        if (fromCellType == XSSFCell.CELL_TYPE_NUMERIC) {
            if (XSSFDateUtil.isCellDateFormatted(fromCell)) {
                toCell.setCellValue(fromCell.getDateCellValue());
            } else {
                toCell.setCellValue(fromCell.getNumericCellValue());
            }
        } else if (fromCellType == XSSFCell.CELL_TYPE_STRING) {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_BLANK) {

        } else if (fromCellType == XSSFCell.CELL_TYPE_BOOLEAN) {
            toCell.setCellValue(fromCell.getBooleanCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_ERROR) {
            toCell.setCellErrorValue(fromCell.getErrorCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_FORMULA) {
            toCell.setCellFormula(fromCell.getCellFormula());
        } else {
        }

    }


    private static void copyRow(Workbook wb, Row oldRow, Row toRow) {
        toRow.setHeight(oldRow.getHeight());
        for (Iterator cellIt = oldRow.cellIterator(); cellIt.hasNext(); ) {
            Cell tmpCell = (XSSFCell) cellIt.next();
            Cell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, tmpCell, newCell);
        }
    }

    private static void copyCellStyle(CellStyle fromStyle, CellStyle toStyle) {

        toStyle.cloneStyleFrom(fromStyle);//此一行代码搞定
    }
}
