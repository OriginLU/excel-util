package com.chl.excel.util;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.constant.CellStyleConstant;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.ExcelCreateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private static int CELL_WIDTH = 20;

    private static ConcurrentMap<String,CellStyle> CELL_STYLE       =       new ConcurrentHashMap();

    public static Workbook createExcel(List list, Class type){

        if (type.getAnnotation(Excel.class) == null) {

        }
        int length = list.size();
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);

        Sheet sheet = workbook.createSheet(titleName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);

        int rowNum = createTitleRow(workbook,sheet,titleName,length);
        rowNum = createColumnName(workbook,sheet,conf,rowNum);
        CountDownLatch latch = new CountDownLatch(length - 1);
        for (int i = rowNum , j = 0; i < length; i++ , j++) {
//            createDataRow(sheet.createRow(i), list.get(j), conf);
            createRowTask(sheet.createRow(i), list.get(j), conf,latch);
        }
//        return workbook;
        return getWorkBook(workbook,latch);
    }

    private static int createColumnName(Workbook workbook, Sheet sheet, ExcelColumnConf[] conf, int rowNum) {

        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < conf.length; i++) {
            Map<Class, Annotation> annotations = conf[i].getAnnotations();
            ExcelColumn excelColumn = (ExcelColumn) annotations.get(ExcelColumn.class);
            String columnName = excelColumn.columnTitle();
            row.createCell(i).setCellValue(columnName);

        }
        return (rowNum + 1);
    }

    private static Workbook getWorkBook(Workbook workbook, CountDownLatch latch) {

        try {
            latch.await();
            return workbook;
        }catch (InterruptedException e){
            throw new ExcelCreateException("",e);
        }
    }

    private static int createTitleRow(Workbook book,Sheet sheet,String titleName,int columnLength) {

        if (StringUtils.isNotBlank(titleName)){
            Row titleRow = sheet.createRow(0);
            Cell cell = titleRow.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnLength - 1));
            cell.setCellStyle(getTitleCellStyle(book));
            cell.setCellValue(titleName);
            return 1;
        }
        return 0;
    }


    private static class ExecutorServiceFactory{

        private static ExecutorService executorService;

        public synchronized static ExecutorService getExecutorInstance(){

            if (null != executorService){
                return executorService;
            }
            int coreCount = Runtime.getRuntime().availableProcessors();
            executorService = Executors.newFixedThreadPool(coreCount);
            return executorService;

        }

        public synchronized static void close(){

            if (executorService != null){
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
            }
        }

    }

    private static void createRowTask(final Row row, final Object obj, final ExcelColumnConf[] configs,final CountDownLatch latch){

        ExecutorService executorService = ExecutorServiceFactory.getExecutorInstance();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                createDataRow(row,obj,configs);
                latch.countDown();
            }
        });
    }


    private static void createRowTask(final CountDownLatch latch){

        ExecutorService executorService = ExecutorServiceFactory.getExecutorInstance();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " : " + Math.random());
                latch.countDown();
            }
        });
    }

    public static void main(String[] args){

        CountDownLatch latch = new CountDownLatch(2000);
        for (int i = 0; i < 2000; i++) {
            createRowTask(latch);
        }
        try {

            latch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
        ExecutorServiceFactory.close();
    }



    /**
     * 设置标题样式
     * @param wb
     * @return
     */
    private static CellStyle getTitleCellStyle(Workbook wb) {

        CellStyle titleStyle;
        if ((titleStyle = CELL_STYLE.get(CellStyleConstant.TITLE_STYLE)) != null){
            return titleStyle;
        }

        Font ztFont = wb.createFont();
        ztFont.setFontName("宋体");                                  // 将“宋体”字体应用到当前单元格上
        ztFont.setItalic(false);                                    // 设置字体为斜体字
        ztFont.setStrikeout(false);                                 // 是否添加删除线
        ztFont.setColor(Font.COLOR_NORMAL);                         // 将字体设置为“红色”
        ztFont.setUnderline(Font.U_SINGLE);                         // 添加（Font.U_SINGLE单条下划线/Font.U_DOUBLE双条下划线）
        ztFont.setFontHeightInPoints((short) 16);                   // 将字体大小设置为18px
        ztFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);             // 加粗

        titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        titleStyle.setFont(ztFont);

        CELL_STYLE.putIfAbsent(CellStyleConstant.TITLE_STYLE,titleStyle);
        return titleStyle;
    }


    /**
     * 设置单元格样式
     * @param wb
     * @return
     */
    private static CellStyle getContentCellStyle(Workbook wb) {

        CellStyle cellStyle;
        if ((cellStyle = CELL_STYLE.get(CellStyleConstant.CONTENT_STYLE)) != null){
            return cellStyle;
        }
        Font cellFont = wb.createFont();
        cellFont.setItalic(false);                                      // 设置字体为斜体字
        cellFont.setFontName("宋体");                                    // 字体应用到当前单元格上
        cellFont.setColor(Font.COLOR_NORMAL);                           // 将字体设置为“红色”
        cellFont.setFontHeightInPoints((short)10);                      // 将字体大小设置为18px
        cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        cellStyle = wb.createCellStyle();                     //表格样式
        cellStyle.setFont(cellFont);
        cellStyle.setWrapText(true);                                    //设置自动换行
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);              //上边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);             //左边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);            //右边框
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);           //下边框
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        CELL_STYLE.putIfAbsent(CellStyleConstant.CONTENT_STYLE,cellStyle);  //等待样式设置完成后再放入容器，防止其他线程取到未设置好的样式

        return cellStyle;
    }

    private static void createDataRow(Row row, Object obj, ExcelColumnConf[] configs){

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
        }catch (InvocationTargetException e){
            throw new ExcelCreateException("invoke method error ",e);
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


}
