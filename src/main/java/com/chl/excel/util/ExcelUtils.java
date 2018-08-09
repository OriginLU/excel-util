package com.chl.excel.util;

import com.chl.excel.annotation.Excel;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.constant.CellStyleConstant;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.RepeatOrderExcetion;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private static int CELL_WIDTH = 20;

    private static ConcurrentMap<String,CellStyle> CELL_STYLE       =       new ConcurrentHashMap();

    public static Workbook createSXSSWorkBook(List list, Class type) throws Exception {

        if (type.getAnnotation(Excel.class) == null) {

        }
        String titleName = ExcelConfigureUtil.getExcelTitleName(type);
        String excelVersion = ExcelConfigureUtil.getExcelVersion(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);

        Sheet sheet = workbook.createSheet(titleName);
        int rowNum = createTitleRow(workbook,sheet,titleName,conf.length);
        for (int i = rowNum, length = list.size(), j = 0; i < length; i++,j++) {
            sheet.setDefaultColumnWidth(CELL_WIDTH);
            createDataRow(sheet.createRow(i), list.get(j), conf);
        }
        return workbook;
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

    private static void createDataRow(Row row, Object obj, ExcelColumnConf[] configs) throws InvocationTargetException {

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
    }

    private static String convertToString(Object result, Map<Class, Annotation> ans) {
        if (result == null) {
            return "";
        }
        return null;
    }


    private static CellStyle getCellStyle(SXSSFWorkbook workbook) {


        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        cellStyle.setFont(font);

        return cellStyle;
    }



    public static void main(String[] args) {

        try {
            Set<Integer> orderSets = new HashSet();
            LinkedList<Integer> index = new LinkedList();
            List<Integer> list = Arrays.asList(0, 6, 2, 5, -1, -1, 4, 8,7);
            Integer[] conf = new Integer[list.size()];
            long start = System.currentTimeMillis();
            for (int i = 0, length = list.size(); i < length; i++) {
                Integer order = list.get(i);
                if (order > -1) {
                    if (orderSets.contains(order)) {
                        throw new RepeatOrderExcetion("the order must not be repeated, the repeat order is " + order);
                    }
                    orderSets.add(order);
                } else {
                    order = (index.size() > 0) ? index.pop() : getFreeIndex(i, conf);
                }
                if (conf[order] != null) {
                    Integer tempIndex = (index.size() > 0) ? index.pop() : getFreeIndex(i, conf);
                    Integer temp = conf[order];
                    conf[order] = order;
                    conf[tempIndex] = tempIndex;
                } else {
                    conf[order] = order;
                }
                if (!index.contains(order = getFreeIndex(i, conf)) && i != length - 1)
                    index.add(order);
            }
            System.out.println("time : " + (System.currentTimeMillis() - start));
            System.out.println(Arrays.toString(conf));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static Integer getFreeIndex(Integer index, Integer[] conf) {

        if (index < conf.length - 1 && conf[index] != null) {
            index = getFreeIndex(index + 1, conf);
        }
        return index;
    }


}
