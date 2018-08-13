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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private  static Logger          log         =        LoggerFactory.getLogger(ExcelUtils.class);


    private static int CELL_WIDTH = 20;

    private static int SHEET_COUNT  =   500;

    private static ConcurrentMap<String, CellStyle> CELL_STYLE = new ConcurrentHashMap();

    private static Sequence  sequence                          = new Sequence(1l,1l);



    public static Workbook createExcel(List list, Class type) {

        return createExcel(list,type,true,true);
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

        if (isCreateColumnName){
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

        if (isCreateTitle){
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

        CellStyle titleStyle;
        if ((titleStyle = CELL_STYLE.get(CellStyleConstant.TITLE_STYLE)) != null) {
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

        CELL_STYLE.putIfAbsent(CellStyleConstant.TITLE_STYLE, titleStyle);
        return titleStyle;
    }


    /**
     * create executor service by ExecutorFactory
     */
    private static class ExecutorFactory{

        private static ExecutorService executorService;

        private static int coreCount = Runtime.getRuntime().availableProcessors();

        public static synchronized ExecutorService getInstance(){

            if (executorService != null && !executorService.isShutdown()){
                return executorService;
            }
            executorService = Executors.newFixedThreadPool(coreCount,new ExecutorThreadFactory());
            return executorService;
        }

        public static synchronized void close(){

            if (executorService != null && executorService.isShutdown()){
                executorService.shutdown();
            }
        }

        /**
         * when you use the {@link ExecutorService#execute(Runnable)}
         * throw exception will be catch by the handler for exception
         */
        private static class ExecutorExceptionHandler implements Thread.UncaughtExceptionHandler{

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(t.getName() + " : ",e);
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




    public static void close(){
        ExecutorFactory.close();
    }

    public static Workbook createExcelAdvance(final List list, final Class type){

        ExecutorService executorService = ExecutorFactory.getInstance();
        List<Future<String>> futures = new ArrayList();

        final String path = getSystemPath();
        String temp = sequence.nextId().toString();             // create temp file name by sequence,it should be only one
        int cycleCount = getCycleCount(list.size());
        for (int i = 0; i < cycleCount; i++) {
            final List nextList = getNextList(list, i);
            final String name = path + temp + "_" +  i + ".xls";
            Future<String> future = executorService.submit(createCallableTask(nextList,type,name));
            futures.add(future);
        }
        for (Future<String> future : futures) {

            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
//        return getWorkBook(futures,type);

    }

    private static Callable<String> createCallableTask(final List list, final Class type, final String name){

        return new Callable<String>() {
            @Override
            public String call(){
                try {
                    Workbook excel = createExcel(list, type);
                    FileOutputStream fos = new FileOutputStream(name);
                    excel.write(fos);
                    fos.flush();
                    fos.close();
                    return name;
                }catch (IOException e){
                    log.error("create file error : ",e);
                    throw new ExcelCreateException("create file error : ",e);
                }
            }
        };
    }

    private static String getSystemPath() {
        return "d:/excel/";
//        return System.getProperty("java.io.tmpdir");
    }

    public static void main(String[] args){


    }

    private static Workbook getWorkBook(List<Future<Workbook>> futures, Class type) {

        try {
            String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
            Workbook workBook = WorkBookFactory.createWorkBook(excelVersion);
            for (Future<Workbook> future : futures) {
                Workbook wk = future.get();
                int numberOfSheets = wk.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = wk.getSheetAt(i);
                    Sheet copySheet = workBook.createSheet(sheet.getSheetName());
                }
            }
            return null;
        }catch (Exception e){
            log.error("create excel error : " ,e);
            throw new ExcelCreateException("create excel error : ", e);
        }
    }

    private static List getNextList(List list,int index){

        int length = list.size();                           // collection size
        int startIndex = index * SHEET_COUNT;
        int endIndex = startIndex + SHEET_COUNT;
        endIndex = length > endIndex ? endIndex : length;
        return list.subList(startIndex, endIndex);
    }

    private static int getCycleCount(int size) {

        int cycleCount = 0;
        if ((size % SHEET_COUNT) != 0){
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

        CellStyle cellStyle;
        if ((cellStyle = CELL_STYLE.get(CellStyleConstant.CONTENT_STYLE)) != null) {
            return cellStyle;
        }
        Font cellFont = wb.createFont();
        cellFont.setItalic(false);                                      // 设置字体为斜体字
        cellFont.setFontName("宋体");                                    // 字体应用到当前单元格上
        cellFont.setColor(Font.COLOR_NORMAL);                           // 将字体设置为“红色”
        cellFont.setFontHeightInPoints((short) 10);                      // 将字体大小设置为18px
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

        CELL_STYLE.putIfAbsent(CellStyleConstant.CONTENT_STYLE, cellStyle);  //等待样式设置完成后再放入容器，防止其他线程取到未设置好的样式

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


}
