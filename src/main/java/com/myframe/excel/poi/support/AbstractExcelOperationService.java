package com.myframe.excel.poi.support;

import com.myframe.excel.converter.DefaultFormatterConverter;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.formatter.DataFormatter;
import com.myframe.excel.poi.cellstyle.DefaultCellStyle;
import com.myframe.excel.poi.cellstyle.POICellStyle;
import com.myframe.excel.util.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractExcelOperationService implements ExcelOperationService {



    private final static TypeDescriptor TARGET_TYPE = TypeDescriptor.valueOf(String.class);

    private TypeConverter converter;

    protected POICellStyle poiCellStyle;


    public AbstractExcelOperationService() {

        this(new DefaultCellStyle());
    }

    public AbstractExcelOperationService(POICellStyle poiCellStyle) {

        this.poiCellStyle = poiCellStyle;
        converter = new DefaultFormatterConverter();
    }

    protected Sheet createSheet(Workbook workbook, String titleName, Class type) {

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        Sheet sheet = workbook.createSheet(sheetName);

        return sheet;
    }

    protected  String convertToString(Object source, Object target, ExcelColumnConfiguration conf) {

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
        if (converter.canConvert(sourceType,TARGET_TYPE))
        {
            return (String) converter.convertValue(target,sourceType,TARGET_TYPE);
        }

        throw new ExcelCreateException("can't convert " + target.getClass() + "to string");
    }


    protected  Object convertValue(ExcelColumnConfiguration conf, Object cellValue) {

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

        if (converter.canConvert(sourceType,targetType))
        {
            return converter.convertValue(cellValue,sourceType,targetType);
        }
        throw new ExcelCreateException("can't convert to " + field.getType().getName());

    }


    protected Object getValue(Object obj, ExcelColumnConfiguration conf) {

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


    protected Object getCellValue(Cell cell){


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
}
