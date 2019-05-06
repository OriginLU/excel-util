package com.myframe.excel.util;

import com.myframe.excel.configure.ExcelConfigurationLoader;
import com.myframe.excel.converter.DefaultFormatterConverter;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.formatter.DataFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author LCH
 * @since 2018-06-13
 */
public abstract class POIUtils {

    private final static int CELL_WIDTH = 20;

    private final static TypeConverter CONVERTER = new DefaultFormatterConverter();

    private final static TypeDescriptor TARGET_TYPE = TypeDescriptor.valueOf(String.class);


    public static Workbook createExcel(List<?> list, Class<?> type) {

        String titleName = ExcelConfigurationLoader.getExcelTitleName(type);
        String excelVersion = ExcelConfigurationLoader.getExcelVersion(type);
        ExcelColumnConfiguration[] conf = ExcelConfigurationLoader.getExportConfiguration(type);
        Workbook workbook = WorkBookFactory.createWorkBook(excelVersion);
        Sheet sheet = createSheet(workbook, titleName,type);
        int rowIndex = createTitleRow(workbook, sheet, titleName, conf.length);
        rowIndex = createColumnNameRow(workbook, sheet, conf, rowIndex);
        createContentRow(workbook, sheet, list, conf, rowIndex);
        return workbook;
    }


    private static Sheet createSheet(Workbook workbook, String titleName,Class type) {

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(CELL_WIDTH);
        return sheet;
    }

    /**
     * create column name for excel
     */
    private static int createColumnNameRow(Workbook workbook, Sheet sheet, ExcelColumnConfiguration[] configs, int rowNum) {

        Row row = sheet.createRow(rowNum);
        CellStyle contentCellStyle = getColumnNameCellStyle(workbook);
        for (int col = 0; col < configs.length; col++)
        {
            String columnName = configs[col].getColumnName();
            Cell cell = row.createCell(col);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(columnName);
        }
        rowNum += 1;
        sheet.createFreezePane(0,rowNum,0,rowNum);
        return rowNum;
    }


    /**
     * create cell data for per row
     */
    private static void createContentRow(Workbook workbook, Sheet sheet, List<?> list, ExcelColumnConfiguration[] configs, int rowNum) {


        int length = list.size();
        int columnLength = configs.length;
        CellStyle cellStyle = getCellStyle(workbook);

        for (int rowIndex = rowNum, data = 0; data < length; rowIndex ++, data ++)
        {
            Row row = sheet.createRow(rowIndex);
            Object obj = list.get(data);
            for (int col = 0; col < columnLength; col++)
            { //create cell for row
                ExcelColumnConfiguration config = configs[col];
                Object result = getValue(obj,config);
                Cell cell = row.createCell(col);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(convertToString(obj,result, config));
            }
        }

    }


    private static String convertToString(Object source,Object target,ExcelColumnConfiguration conf) {

        if (target == null)
        {
            return conf.getDefaultValue();
        }

        DataFormatter formatter = conf.getFormatter();
        if (formatter != null)
        {
            return formatter.format(source,target);
        }

        TypeDescriptor sourceType = conf.getTypeDescriptor();
        if (CONVERTER.canConvert(sourceType,TARGET_TYPE))
        {
            return (String) CONVERTER.convertValue(target,sourceType,TARGET_TYPE);
        }

        throw new ExcelCreateException("can't convert " + target.getClass() + "to string");
    }


    private static Object getValue(Object obj, ExcelColumnConfiguration conf) {

        try
        {
            Field field = conf.getField();

            if (field != null)
            {
                return ReflectUtils.getFieldValue(obj, field);
            }

            Method method = conf.getMethod();
            if (method != null)
            {
                return ReflectUtils.invokeMethod(obj,method);
            }

            return null;
        }
        catch (Exception e)
        {
            throw new ExcelCreateException("create excel error ", e);
        }
    }

    /**
     * create title
     */
    private static int createTitleRow(Workbook book, Sheet sheet, String titleName, int columnLength) {


        if (StringUtils.isNotBlank(titleName))
        {
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


    public static List<?> importExcel(File file,Class<?> type){

        try {
            Workbook workbook = getWorkBook(file);

            if (workbook != null)
            {
                List<Object> results = new ArrayList<>();
                ExcelColumnConfiguration[] importConfiguration = ExcelConfigurationLoader.getImportConfiguration(type);
                for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++)
                {
                    Sheet sheet = workbook.getSheetAt(sheetNum);
                    if(sheet != null)
                    {
                        int firstRowNum  = sheet.getFirstRowNum();
                        int lastRowNum = sheet.getLastRowNum();

                        for(int rowNum = firstRowNum + 1;rowNum <= lastRowNum;rowNum++)
                        {
                            Row row = sheet.getRow(rowNum);
                            if(row != null)
                            {
                                results.add(toObject(row,importConfiguration,type));
                            }
                        }
                    }
                }
                return results;
            }
            return null;
        } catch (Exception e) {
            throw new ExcelCreateException("import excel error ",e);
        }

    }


    private static Object toObject(Row row, ExcelColumnConfiguration[] importConfiguration, Class<?> clazz) throws IllegalAccessException, InstantiationException {


        Object target = clazz.newInstance();
        int firstCellNum = row.getFirstCellNum();
        int lastCellNum = row.getPhysicalNumberOfCells();

        for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++)
        {
            Cell cell = row.getCell(cellNum);
            Object cellValue = getCellValue(cell);

            ExcelColumnConfiguration conf = importConfiguration[cellNum];
            Object convertValue = convertValue(conf,cellValue);
            ReflectUtils.setFieldValue(conf.getField(),target,convertValue);
        }
        return target;
    }


    private static Object convertValue(ExcelColumnConfiguration conf, Object cellValue) {

        if (null == cellValue)
        {
            return null;
        }

        DataFormatter formatter = conf.getFormatter();
        if (formatter != null)
        {
            return formatter.convertValue(cellValue, conf);
        }

        Field field = conf.getField();

        TypeDescriptor sourceType = TypeDescriptor.forObject(cellValue);
        TypeDescriptor targetType = conf.getTypeDescriptor();

        if (CONVERTER.canConvert(sourceType,targetType))
        {
            return CONVERTER.convertValue(cellValue,sourceType,targetType);
        }
        throw new ExcelCreateException("can't convert to " + field.getType().getName());

    }


    private static Workbook getWorkBook(File file) {
        return null;
    }


    private static Object getCellValue(Cell cell){


        if(cell == null)
        {
            return null;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
        {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }

        switch (cell.getCellType())
        {
            //数字
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();

            //字符串
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();

            //Boolean
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();

            //公式
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();

            //空值
            case Cell.CELL_TYPE_BLANK:
            //故障
            case Cell.CELL_TYPE_ERROR:
            default:
                return "";
        }
    }


    /**
     * merge excel
     */
    public static void mergeExcel(Workbook fromWorkBook, Workbook toWorkBook, String sheetName) {

        try
        {
            for (int i = 0; i < fromWorkBook.getNumberOfSheets(); i++)
            {
                Sheet oldSheet = fromWorkBook.getSheetAt(i);
                Sheet newSheet = toWorkBook.createSheet(sheetName);
                copySheet(toWorkBook, oldSheet, newSheet);
            }
        }
        catch (Exception e)
        {
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
            toCell.setCellValue(fromCell.getStringCellValue());
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

        toStyle.cloneStyleFrom(fromStyle);
    }


}
