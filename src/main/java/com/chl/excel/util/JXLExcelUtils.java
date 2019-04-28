package com.chl.excel.util;

import com.chl.excel.configure.ExcelConfigurationLoader;
import com.chl.excel.entity.ExcelColumnConf;
import com.chl.excel.exception.ExcelCreateException;
import jxl.CellView;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author lch
 * @since 2018-08-15
 */
public class JXLExcelUtils extends BaseUtils {


    public static byte[] createExcel(List list, Class type) {

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ExcelColumnConf[] conf = ExcelConfigurationLoader.getExcelColConfiguration(type);
            String titleName = ExcelConfigurationLoader.getExcelTitleName(type);
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            WritableSheet sheet = createDefaultSheet(workbook,titleName,type);
            int rowIndex = createTitleRow(sheet, titleName, conf.length);
            rowIndex = createColumnTitleRow(sheet, conf, rowIndex);
            createContentRow(sheet, list, conf, rowIndex);
            workbook.write();
            workbook.close();
            return os.toByteArray();
        }catch (Exception e){
            throw new ExcelCreateException("create title row has error ", e);
        }
    }

    private static WritableSheet createDefaultSheet(WritableWorkbook workbook, String titleName, Class type){

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        return workbook.createSheet(sheetName, 0);
    }

    /**
     * 创建标题
     *
     */
    private static int createTitleRow(WritableSheet sheet, String titleName, int columnLength) {

        try {
            if (StringUtils.isNotBlank(titleName)) {
                SheetSettings settings = sheet.getSettings();
                settings.setVerticalFreeze(0);                                      //设置冻结行
                Label label = new Label(0, 0, titleName, getTitleCellFormat());
                sheet.mergeCells(0, 0, columnLength - 1, 0);
                sheet.addCell(label);
                return 1;
            }
            return 0;
        } catch (Exception e) {
            throw new ExcelCreateException("create title row has error ", e);
        }
    }

    /**
     * 创建列标题
     */
    private static int createColumnTitleRow(WritableSheet sheet, ExcelColumnConf[] configs, int rowNum) {

        CellFormat cellFormat = getColumnTitleCellFormat();
        try {
            for (int col = 0; col < configs.length; col++) {
                String columnName = getColumnName(configs[col]);
                Label label = new Label(col, rowNum, columnName, cellFormat);
                sheet.addCell(label);
            }
        } catch (Exception e) {
            throw new ExcelCreateException("create excel the title of column has error ", e);
        }
        rowNum  = rowNum + 1;
        SheetSettings settings = sheet.getSettings();
        settings.setVerticalFreeze(rowNum);
        return rowNum;
    }

    /**
     * 创建数据行
     */
    private static void createContentRow(WritableSheet sheet, List list, ExcelColumnConf[] configs, int rowNum) {


        int length = list.size();
        int columnLength = configs.length;
        CellView cellView = getCellView();
        CellFormat cellFormat = getColumnCellFormat();
        try {
            for (int row = rowNum, data = 0; data < length; row++, data++) {
                Object obj = list.get(data);
                for (int col = 0; col < columnLength; col++) { //create cell for row
                    ExcelColumnConf config = configs[col];
                    Object result = getValue(obj,config);
                    String s = convertToString(result, config.getAnnotations());
                    Label label = new Label(col,row,s, cellFormat);
                    sheet.addCell(label);
                    sheet.setColumnView(col, cellView);
                }
            }
        } catch (Exception e) {
            throw new ExcelCreateException("excel create error", e);
        }

    }

    /**
     * 设置自动宽度
     *
     */
    private static CellView getCellView() {

        CellView cellView = new CellView();
        cellView.setAutosize(true); //设置自动大小
        return cellView;
    }

    /**
     * 设置标题样式
     *
     */
    private static CellFormat getTitleCellFormat() {


        try {
            WritableFont font = new WritableFont(WritableFont.ARIAL);      //设置字体;

            font.setBoldStyle(WritableFont.BOLD);                         //字体加粗
            font.setUnderlineStyle(UnderlineStyle.SINGLE);                //设置下划线
            font.setPointSize(15);
            font.setItalic(false);                                        //斜体

            return getCellFormat(font);
        } catch (Exception e) {
            throw new ExcelCreateException("create excel error", e);
        }

    }

    /**
     * 设置列标题样式
     *
     * @return
     */
    private static CellFormat getColumnTitleCellFormat() {

        try {

            WritableFont font = new WritableFont(WritableFont.ARIAL); //设置字体;

            font.setBoldStyle(WritableFont.BOLD);                   //字体加粗
            font.setUnderlineStyle(UnderlineStyle.NO_UNDERLINE);          //设置下划线
            font.setPointSize(12);
            font.setItalic(false);

            return getCellFormat(font);
        } catch (Exception e) {
            throw new ExcelCreateException("create excel error", e);
        }
    }

    /**
     * 设置列样式
     *
     * @return
     */
    private static CellFormat getColumnCellFormat() {

        try {

            WritableFont font = new WritableFont(WritableFont.ARIAL); //设置字体;

            font.setBoldStyle(WritableFont.NO_BOLD);                   //字体加粗
            font.setUnderlineStyle(UnderlineStyle.NO_UNDERLINE);          //设置下划线
            font.setPointSize(10);
            font.setItalic(false);

            return getCellFormat(font);
        } catch (Exception e) {
            throw new ExcelCreateException("create excel error", e);
        }
    }

    private static CellFormat getCellFormat(WritableFont font) throws WriteException {

        WritableCellFormat cellFormat = new WritableCellFormat(font);
        cellFormat.setAlignment(Alignment.CENTRE);                    //设置文字居中对齐方式
        cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);    //设置垂直居中
        return cellFormat;

    }
}
