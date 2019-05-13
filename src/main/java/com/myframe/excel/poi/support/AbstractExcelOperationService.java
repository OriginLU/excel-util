package com.myframe.excel.poi.support;

import com.myframe.common.utils.Sequence;
import com.myframe.excel.converter.DefaultFormatterConverter;
import com.myframe.excel.entity.ExcelColumnConfiguration;
import com.myframe.excel.entity.ExcelConfiguration;
import com.myframe.excel.exception.ExcelCreateException;
import com.myframe.excel.formatter.DataFormatter;
import com.myframe.excel.loader.ConfigurationLoader;
import com.myframe.excel.loader.impl.conf.ExcelConfigurationLoader;
import com.myframe.excel.poi.cellstyle.DefaultCellStyle;
import com.myframe.excel.poi.cellstyle.POICellStyle;
import com.myframe.excel.util.FileUtils;
import com.myframe.excel.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractExcelOperationService implements ExcelOperationService {



    private final static TypeDescriptor TARGET_TYPE = TypeDescriptor.valueOf(String.class);

    private TypeConverter converter;

    private Sequence sequence;

    protected POICellStyle poiCellStyle;

    protected ConfigurationLoader configurationLoader;


    public AbstractExcelOperationService() {

        this(new DefaultCellStyle());
    }

    public AbstractExcelOperationService(POICellStyle poiCellStyle) {

        this.poiCellStyle = poiCellStyle;
        this.converter = new DefaultFormatterConverter();
        this.sequence = new Sequence(0L,0L);
        this.configurationLoader = ExcelConfigurationLoader.getExcelConfigurationLoader();
    }

    protected Sheet createSheet(Workbook workbook, String titleName, Class type) {

        String sheetName = StringUtils.isBlank(titleName) ? type.getSimpleName() : titleName;
        return workbook.createSheet(sheetName);
    }

    protected String writeToLocal(Workbook workbook,Class<?> type, String path){


        FileOutputStream fos = null;
        try
        {
            String fullPath = getFullPath(type,path);
            fos = new FileOutputStream(fullPath);
            workbook.write(fos);
            fos.flush();

            return fullPath;
        }
        catch (Throwable e)
        {
            throw new ExcelCreateException("can't create excel file,check please");
        }
        finally
        {
            close(fos);
        }

    }

    private String getFullPath(Class<?> type, String path) {
        path = FileUtils.getPath(path);
        ExcelConfiguration exportConfiguration = configurationLoader.getExportConfiguration(type);
        String fileSuffix = exportConfiguration.getFileSuffix();
        String excelName = getOutName(exportConfiguration.getExcelName(),type);
        return path + excelName + exportConfiguration.getFileSuffix();
    }

    private void close(Closeable closeable)
    {
        if (closeable != null)
        {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new ExcelCreateException("can't close output stream,check please");
            }
        }
    }

    private String getOutName(String name,Class<?> type){

        if (StringUtils.isNotBlank(name))
        {
            return name + "-" + sequence.nextId();
        }
        return type.getSimpleName() + "-" + sequence.nextId();

    }


    protected List<?> getNextList(List<?> totalList, int count,int separateRowNum) {

        int size = totalList.size();

        int start = separateRowNum * count;
        int end = start + separateRowNum;
        end = end < size ? end : size;

        return totalList.subList(start, end);
    }

    protected int getCycleCount(int totalCount,int separateRowNum)
    {
        if ((totalCount % separateRowNum) != 0)
        {
            return (totalCount / separateRowNum) + 1;
        }
        return totalCount / separateRowNum;
    }


    protected  String convertToString(Object source, Object target, ExcelColumnConfiguration conf) {

        if (target == null)
        {
            return conf.getDefaultValue();
        }

        try {

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

            throw new ConverterNotFoundException(sourceType, TARGET_TYPE);

        }
        catch (Throwable e)
        {
            throw new ExcelCreateException("the " + conf.getColumnName() +" convert occur error,the value is [" + target +"]",e);
        }
    }


    protected  String convertToString(Object target) {

        if (target == null)
        {
            return "";
        }

        TypeDescriptor sourceType = TypeDescriptor.forObject(target);
        if (converter.canConvert(sourceType,TARGET_TYPE))
        {
            String convertValue = (String) converter.convertValue(target, sourceType, TARGET_TYPE);

            return convertValue.trim();
        }

        return "";

    }


    protected  Object convertValue(ExcelColumnConfiguration conf, Object cellValue) {

        if (null == cellValue)
        {
            return null;
        }

        try
        {

            DataFormatter formatter = conf.getFormatter();
            if (formatter != null)
            {
                return formatter.convertValue(convertToString(cellValue));
            }

            TypeDescriptor sourceType = TypeDescriptor.forObject(cellValue);
            TypeDescriptor targetType = conf.getTypeDescriptor();

            if (converter.canConvert(sourceType,targetType))
            {
                return converter.convertValue(cellValue,sourceType,targetType);
            }

            throw new ConverterNotFoundException(sourceType,targetType);
        }
        catch (Throwable e)
        {
            throw new ExcelCreateException("the " + conf.getColumnName() +" convert occur error,the value is [" + cellValue +"]",e);
        }

    }


    protected Object getValue(Object obj, ExcelColumnConfiguration conf) {

        try
        {
            Field field = conf.getField();

            if (field != null)
            {
                return ReflectionUtils.getFieldValue(obj, field);
            }

            Method method = conf.getMethod();
            if (method != null)
            {
                return ReflectionUtils.invokeMethod(obj,method);
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
