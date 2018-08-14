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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * because of poi as the tool of excel for operation is not be safe in the multi thread,
 * you can overwrite the {@link Row#createCell(int)} and {@link Sheet#createRow(int)} method
 * and add lock for guarantee safe operation,but it performance bad than single thread
 *
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private final static Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private static int CELL_WIDTH = 20;

    private static int SHEET_COUNT = 10000;


    private static Object getResult(ExcelColumnConf config, Object obj) throws InvocationTargetException {

        Field field = config.getAnnotationField();
        if (field != null) {
            return ReflectUtils.getFieldValue(obj, field);
        }
        Method method = config.getAnnotationMethod();
        if (method != null) {
            return ReflectUtils.invokeMethod(obj, method);
        }
        return null;
    }

    public static Workbook createExcel(List list, Class type) {

        if (type.getAnnotation(Excel.class) == null) {

        }
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);
        Sheet sheet = workbook.createSheet(titleName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);

        int rowNum = 0;
        int columnLength = conf.length;

        rowNum = createTitleRow(workbook, sheet, titleName, columnLength);
        rowNum = createColumnNameRow(workbook, sheet, conf, rowNum);
        createContentRow(workbook, sheet, list, conf, rowNum);

        return workbook;
    }

    /**
     * create cell data for per row
     * @param workbook
     * @param sheet
     * @param list
     * @param configs
     * @param rowNum
     */
    private static void createContentRow(Workbook workbook, Sheet sheet, List list, ExcelColumnConf[] configs, int rowNum) {

        try {
            int length = list.size();
            int columnLength = configs.length;
            CellStyle cellStyle = getContentCellStyle(workbook);
            for (int i = rowNum, j = 0; j < length; i++, j++) {
                Row row = sheet.createRow(i);
                Object obj = list.get(j);
                for (int k = 0; k < columnLength; k++) {
                    ExcelColumnConf config = configs[k];
                    Object result = getResult(config, obj);
                    Cell cell = row.createCell(k);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(convertToString(result, config.getAnnotations()));
                }
            }
        } catch (InvocationTargetException e) {
            log.error("invoke method throw exception : ", e);
            throw new ExcelCreateException("invoke method error ", e);
        }
    }

    /**
     * create column name for excel
     * @param workbook
     * @param sheet
     * @param conf
     * @param rowNum
     * @return
     */
    private static int createColumnNameRow(Workbook workbook, Sheet sheet, ExcelColumnConf[] conf, int rowNum) {

        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < conf.length; i++) {
            Map<Class, Annotation> annotations = conf[i].getAnnotations();
            ExcelColumn excelColumn = (ExcelColumn) annotations.get(ExcelColumn.class);
            String columnName = excelColumn.columnTitle();
            row.createCell(i).setCellValue(columnName);

        }
        return (rowNum + 1);
    }

    /**
     * create title
     * @param book
     * @param sheet
     * @param titleName
     * @param columnLength
     * @return
     */
    private static int createTitleRow(Workbook book, Sheet sheet, String titleName, int columnLength) {


        if (StringUtils.isNotBlank(titleName)) {
            Row titleRow = sheet.createRow(0);
            Cell cell = titleRow.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnLength - 1));
            cell.setCellStyle(getTitleCellStyle(book));
            cell.setCellValue(titleName);
            return 1;
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

    private static String convertToString(Object result, Map<Class, Annotation> ans) {
        if (result == null) {
            return "";
        }
        return result.toString();
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
                e.printStackTrace();
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
}
