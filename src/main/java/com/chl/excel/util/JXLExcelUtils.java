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
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @author lch
 * @since 2018-08-15
 */
public class JXLExcelUtils extends BaseUtils {

    private final static Logger log = LoggerFactory.getLogger(JXLExcelUtils.class);

    private static int CELL_WIDTH = 200;

    private static int SHEET_COUNT = 1000;

    private static Sequence sequence = new Sequence(2l, 2l);

    public static void main(String[] args)
            throws IOException, RowsExceededException, WriteException {
        //1:创建excel文件
        File file = new File("d:/test.xls");
        file.createNewFile();
        //2:创建工作簿
        final WritableWorkbook workbook = Workbook.createWorkbook(file);
        //3:创建sheet,设置第二三四..个sheet，依次类推即可
        ExecutorService executorService = ExecutorFactory.getInstance();

        final CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {

            final WritableSheet sheet = workbook.createSheet("用户管理", i);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    try {

                        //4：设置titles
                        String[] titles = {"编号", "账号", "密码"};
                        //5:单元格
                        Label label = null;
                        //6:给第一行设置列名
                        for (int i = 0; i < titles.length; i++) {
                            //x,y,第一行的列名
                            label = new Label(i, 0, titles[i], getColumnCellFormat());
                            //7：添加单元格
                            sheet.addCell(label);
                        }
                        //8：模拟数据库导入数据
                        for (int i = 1; i < 10; i++) {
                            //添加编号，第二行第一列
                            label = new Label(0, i, i + "");
                            sheet.addCell(label);

                            //添加账号
                            label = new Label(1, i, "10010" + i);
                            sheet.addCell(label);

                            //添加密码
                            label = new Label(2, i, "123456");
                            sheet.addCell(label);
                        }

                        latch.countDown();
                    } catch (Exception e) {
                        throw new ExcelCreateException("create excel error", e);
                    }

                }
            };
            executorService.execute(runnable);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        //最后一步，关闭工作簿
        workbook.close();
        executorService.shutdown();
    }


    public static File createExcel(List list, Class type) {

        try {
            File tempFile = File.createTempFile("test", ".xls");
            ExcelColumnConf[] conf = ExcelConfigureUtil.getExcelColumnConfiguration(type);
            String titleName = ExcelConfigureUtil.getExcelTitleName(type);
            WritableWorkbook workbook = Workbook.createWorkbook(tempFile);
            WritableSheet sheet = workbook.createSheet("用户管理", 0);
            int rowIndex = createTitleRow(sheet, titleName, conf.length);
            rowIndex = createColumnTitleRow(sheet, conf, rowIndex);
            createContentRow(sheet, list, conf, rowIndex);

        }catch (Exception e){
            throw new ExcelCreateException("create title row has error ", e);
        }
        return null;
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
                settings.setVerticalFreeze(0);                              //设置冻结行
                sheet.setColumnView(0, getCellView());                   //设置自动宽度样式
                Label label = new Label(0, 0, titleName, getTitleCellFormat());
                sheet.addCell(label);
                sheet.mergeCells(0, 0, 0, columnLength - 1);
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
            for (int i = 0; i < configs.length; i++) {
                String columnName = getColumnName(configs[i]);
                Label label = new Label(i, rowNum, columnName, cellFormat);
                sheet.addCell(label);
            }
        } catch (Exception e) {
            throw new ExcelCreateException("create excel the title of column has error ", e);
        }
        SheetSettings settings = sheet.getSettings();
        settings.setVerticalFreeze(rowNum);
        return (rowNum + 1);
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
        CellFormat cellFormat = getColumnCellFormat();
        CellView cellView = getCellView();
        try {
            for (int rowIndex = rowNum, dataIndex = 0; dataIndex < length; rowIndex++, dataIndex++) {
                Object obj = list.get(dataIndex);
                for (int cellIndex = 0; cellIndex < columnLength; cellIndex++) { //create cell for row
                    ExcelColumnConf config = configs[cellIndex];
                    Object result = getResult(config, obj);
                    String s = convertToString(result, config.getAnnotations());
                    Label label = new Label(rowIndex, cellIndex, s, cellFormat);
                    sheet.addCell(label);
                    sheet.setColumnView(cellIndex, cellView);
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
        cellFormat.setWrap(true);                                     //设置自动换行
        cellFormat.setAlignment(Alignment.CENTRE);                    //设置文字居中对齐方式
        cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);    //设置垂直居中
        return cellFormat;

    }
}
