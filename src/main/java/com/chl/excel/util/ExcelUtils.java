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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private final static Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private static int CELL_WIDTH = 20;

    private static int SHEET_COUNT = 500;

    private static Sequence sequence = new Sequence(1l, 1l);



    public static Workbook createExcelAdvance(List list, Class type){

        if (type.getAnnotation(Excel.class) == null) {

        }
        int length = list.size();
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);

        Sheet sheet = workbook.createSheet(titleName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);

        int rowNum = createTitleRow(workbook,sheet,titleName,conf.length,true);
        rowNum = createColumnName(workbook,sheet,conf,rowNum,true);

        int cycleCount = getCycleCount(length);
        CountDownLatch latch = new CountDownLatch(cycleCount);

        for (int i = rowNum , j = 0; j < cycleCount; i++ , j++) {
            int start = getStartIndex(cycleCount,i);
            int end = getEndIndex(cycleCount,length,i);
            createRowTask(sheet, list.get(j),conf,latch,start,end);
        }
        return getWorkBook(workbook,latch);
    }

    private static int getEndIndex(int cycleCount, int length, int i) {
        int end = cycleCount * i + SHEET_COUNT;
        return length <= end?  length : end;
    }

    private static int getStartIndex(int cycleCount, int i) {
        return i*cycleCount;
    }


    private static Workbook getWorkBook(Workbook workbook, CountDownLatch latch) {

        try {
            latch.await();
            return workbook;
        }catch (InterruptedException e){
            throw new ExcelCreateException("",e);
        }
    }


    private static void createRowTask(final Sheet sheet, final Object obj, final ExcelColumnConf[] configs,final CountDownLatch latch,final int start,final int end){

        ExecutorService executorService = ExecutorFactory.getInstance();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = start; i < end; i++) {
                        Row row = getRow(sheet, i);
                        createDataRow(row,obj,configs);
                    }
                }finally {
                    latch.countDown();
                }
            }
        });
    }

    public static synchronized Row getRow(Sheet sheet,int index){
        Row row = sheet.createRow(index);
        return row;
    }

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
                setCell(row,result,config,i);
            }
        } catch (InvocationTargetException e) {
            log.error("invoke method throw exception : ", e);
            throw new ExcelCreateException("invoke method error ", e);
        }
    }

    private static synchronized void setCell(Row row,Object result,ExcelColumnConf config,int index){
        row.createCell(index).setCellValue(convertToString(result, config.getAnnotations()));
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
}
