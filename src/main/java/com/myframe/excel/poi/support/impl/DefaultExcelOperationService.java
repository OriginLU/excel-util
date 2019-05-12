package com.myframe.excel.poi.support.impl;

import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.entity.ExcelConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.exception.ExcelImportException;
import com.myframe.excel.poi.cellstyle.POICellStyle;
import com.myframe.excel.poi.support.AbstractExcelOperationService;
import com.myframe.excel.util.ReflectionUtils;
import com.myframe.excel.util.WorkBookFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultExcelOperationService extends AbstractExcelOperationService {


    public DefaultExcelOperationService() {

        super();
    }

    public DefaultExcelOperationService(POICellStyle poiCellStyle) {

        super(poiCellStyle);
    }


    @Override
    public Workbook exportSheet(List<?> data, Class<?> type) {


        ExcelConfiguration exportConfiguration = configurationLoader.getExportConfiguration(type);

        String titleName = exportConfiguration.getExcelName();
        String version = exportConfiguration.getVersion();

        Workbook workbook = WorkBookFactory.createWorkBook(version);
        Sheet sheet = createSheet(workbook,titleName,type);
        doCreateRow(data,workbook,sheet,exportConfiguration);

        return workbook;
    }

    @Override
    public Workbook exportMultiSheet(List<?> data, Class<?> type, int maxRowNum) {

        ExcelConfiguration exportConfiguration = configurationLoader.getExportConfiguration(type);
        String version = exportConfiguration.getVersion();
        String titleName = exportConfiguration.getExcelName();
        Workbook workBook = WorkBookFactory.createWorkBook(version);

        int cycleCount = getCycleCount(data.size(), maxRowNum);
        for (int index = 0; index < cycleCount; index++)
        {
            Sheet sheet = createSheet(workBook, titleName + "-" + index, type);

            List<?> nextList = getNextList(data, index, maxRowNum);

            doCreateRow(nextList,workBook,sheet,exportConfiguration);
        }
        return workBook;
    }

    private void doCreateRow(List<?> data,Workbook workbook,Sheet sheet,ExcelConfiguration configuration)
    {

        String titleName = configuration.getExcelName();
        boolean createTitle = configuration.isCreateTitle();
        ExcelColumnConfiguration[] conf = configuration.getConfigurations();
        int rowIndex = createTitleRow(workbook, sheet, titleName, conf.length,createTitle);
        rowIndex = createColumnNameRow(workbook, sheet, conf, rowIndex);
        createContentRow(workbook, sheet, data, conf, rowIndex);
        autoSizeColumn(sheet,conf.length);
    }


    private int createTitleRow(Workbook book, Sheet sheet, String titleName, int columnLength,boolean isCreateTile) {


        if (StringUtils.isNotBlank(titleName) && isCreateTile)
        {
            Row titleRow = sheet.createRow(0);
            Cell cell = titleRow.createCell(0);
            cell.setCellValue(titleName);
            cell.setCellStyle(poiCellStyle.getTitleCellStyle(book));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnLength - 1));
            sheet.createFreezePane(0,1,0,1);
            return 1;
        }
        return 0;
    }


    private  int createColumnNameRow(Workbook workbook, Sheet sheet, ExcelColumnConfiguration[] configs, int rowNum) {

        Row row = sheet.createRow(rowNum);
        CellStyle contentCellStyle = poiCellStyle.getColumnCellStyle(workbook);
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


    private  void createContentRow(Workbook workbook, Sheet sheet, List<?> list, ExcelColumnConfiguration[] configs, int rowNum) {


        int length = list.size();
        int columnLength = configs.length;
        CellStyle cellStyle = poiCellStyle.getContentCellStyle(workbook);

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

    private void autoSizeColumn(Sheet sheet, int length) {

        for (int i = 0; i < length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @Override
    public List<?> importData(InputStream ins, Class<?> type) {

        try {
            Workbook workbook = WorkbookFactory.create(ins);

            ExcelConfiguration importConfiguration = configurationLoader.getImportConfiguration(type);
            ExcelColumnConfiguration[] configurations = importConfiguration.getConfigurations();
            List<Object> results = new ArrayList<>();

            for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++)
            {
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if(sheet != null)
                {
                    int firstRowNum  = getFirstRowNum(sheet,importConfiguration);
                    int lastRowNum = sheet.getLastRowNum();

                    for(int rowNum = firstRowNum;rowNum <= lastRowNum;rowNum++)
                    {
                        Row row = sheet.getRow(rowNum);
                        if(row != null)
                        {
                            results.add(convertToObject(row, configurations, type));
                        }
                    }
                }
            }
            return results;

        } catch (Exception e) {

            throw new ExcelImportException("import excel error ",e);
        }
    }

    private int getFirstRowNum(Sheet sheet, ExcelConfiguration importConfiguration) {


        int columnLength = 0;
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        String titleName = importConfiguration.getExcelName();
        ExcelColumnConfiguration[] configurations = importConfiguration.getConfigurations();
        int length = configurations.length;
        for (int rowNum = firstRowNum; rowNum < lastRowNum; rowNum ++) {

            Row row = sheet.getRow(rowNum);

            int firstCellNum = row.getFirstCellNum();
            int lastCellNum = row.getPhysicalNumberOfCells();

            for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++)
            {
                Cell cell = row.getCell(cellNum);
                Object cellValue = getCellValue(cell);
                String column = convertToString(cellValue);
                if (titleName.equals(column))
                {
                    continue;
                }

                if (configurations[cellNum].getColumnName().equals(column))
                {
                    columnLength ++;
                }
            }
            if (((columnLength > 0 && columnLength < length) && length == lastCellNum) || length < lastCellNum)
            {
                break;
            }
            else if (columnLength == length)
            {
                return rowNum + 1;
            }
        }
        throw new ExcelImportException("inconsistent number of matching columns,and the match column length is " + columnLength + ",check your upload data please");
    }


    private Object convertToObject(Row row, ExcelColumnConfiguration[] importConfiguration, Class<?> clazz) throws IllegalAccessException, InstantiationException {


        Object target = clazz.newInstance();
        int firstCellNum = row.getFirstCellNum();
        int lastCellNum = row.getPhysicalNumberOfCells();

        for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++)
        {
            Cell cell = row.getCell(cellNum);
            Object cellValue = getCellValue(cell);

            ExcelColumnConfiguration conf = importConfiguration[cellNum];
            Object convertValue = convertValue(conf,cellValue);
            ReflectionUtils.setFieldValue(conf.getField(),target,convertValue);
        }
        return target;
    }

    @Override
    public void merge(Workbook fromWorkBook, Workbook toWorkBook, String sheetName) {

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


    private  void copySheet(Workbook wb, Sheet fromSheet, Sheet toSheet) {

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

    private  void mergeSheetAllRegion(Sheet fromSheet, Sheet toSheet) {//合并单元格
        int num = fromSheet.getNumMergedRegions();
        for (int i = 0; i < num; i++)
        {
            CellRangeAddress cellR = fromSheet.getMergedRegion(i);
            toSheet.addMergedRegion(cellR);
        }
    }

    private  void copyCell(Workbook wb, Cell fromCell, Cell toCell) {

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


    private  void copyRow(Workbook wb, Row oldRow, Row toRow) {

        toRow.setHeight(oldRow.getHeight());

        for (Iterator cellIt = oldRow.cellIterator(); cellIt.hasNext(); )
        {
            Cell tmpCell = (XSSFCell) cellIt.next();
            Cell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, tmpCell, newCell);
        }
    }

    private  void copyCellStyle(CellStyle fromStyle, CellStyle toStyle) {

        toStyle.cloneStyleFrom(fromStyle);
    }
}
