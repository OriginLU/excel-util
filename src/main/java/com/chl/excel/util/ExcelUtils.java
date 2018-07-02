package com.chl.excel.util;

import com.chl.excel.annotation.Excel;
import com.chl.excel.configure.ExcelConfigureUtil;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.RepeatOrderExcetion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author LCH
 * @since 2018-06-13
 */
public abstract class ExcelUtils {


    private static int CELL_WIDTH = 20;


    public static SXSSFWorkbook generateSXSSWorkBook(List list, Class type) throws Exception {

        if (type.getAnnotation(Excel.class) == null) {

        }
        ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        for (int i = 0, length = list.size(); i < length; i++) {
            Sheet sheet = workbook.createSheet("sheet");
            sheet.setDefaultColumnWidth(CELL_WIDTH);
            generateRowData(sheet.createRow(i), list.get(i), conf);
        }
        return workbook;
    }

    private static void generateRowData(Row row, Object obj, ExcelColumnConf[] configs) throws InvocationTargetException {

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
            List<Integer> list = Arrays.asList(0, -1, 2, 5, -1, -1, 4, 8, -1);
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
                    order = (index.size() != 0 && (order = index.pop()) != null) ? order : getFreeIndex(i, conf);
                }
                if (conf[order] != null) {
                    Integer tempIndex = (index.size() != 0 && (tempIndex = index.pop()) != null) ? tempIndex : getFreeIndex(i, conf);
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
