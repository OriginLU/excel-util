package com.chl.excel.util;

import com.chl.excel.configure.ExcelConfigureUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author lch
 * @since 2018-08-15
 */
public class JXLExcelUtils extends BaseUtils {

    private final static Logger log = LoggerFactory.getLogger(JXLExcelUtils.class);

    public static byte[] createExcel(List list, Class type) {

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
            String titleName = ExcelConfigureUtil.getExcelTitleName(type);
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            WritableSheet sheet = createSheet(workbook,titleName,type,0);
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

    private static WritableSheet createSheet(WritableWorkbook workbook,String titleName,Class type,int index){

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        WritableSheet sheet = workbook.createSheet(titleName, index);
        return sheet;
    }

    /**
     * 创建标题
     *
     * @param sheet
     * @param titleName
     * @param columnLength
     * @return
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
     * @param sheet
     * @param configs
     * @param rowNum
     * @return
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
     *
     * @param sheet
     * @param list
     * @param configs
     * @param rowNum
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
                    Object result = getResult(config, obj);
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
     * @return
     */
    private static CellView getCellView() {

        CellView cellView = new CellView();
        cellView.setAutosize(true); //设置自动大小
        return cellView;
    }

    /**
     * 设置标题样式
     *
     * @return
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
