package com.myframe.excel.poi;

import com.myframe.excel.poi.cellstyle.POICellStyle;
import com.myframe.excel.poi.support.ExcelOperationService;
import com.myframe.excel.poi.support.impl.DefaultExcelOperationService;

public class POIFactory {

    private POICellStyle poiCellStyle;

    private static ExcelOperationService excelOperationService;

    public static POIFactory getInstance(){
        return new POIFactory();
    }

    public ExcelOperationService build(){


        if (poiCellStyle == null)
        {
            return getDefaultExcelOperationService();
        }
        return new DefaultExcelOperationService(poiCellStyle);

    }



    public void setPoiCellStyle(POICellStyle poiCellStyle) {
        this.poiCellStyle = poiCellStyle;
    }


    private ExcelOperationService getDefaultExcelOperationService() {

        if (excelOperationService == null)
        {
            synchronized (POIFactory.class)
            {
                if (null == excelOperationService)
                {
                    excelOperationService = new DefaultExcelOperationService();
                }
            }
        }
        return excelOperationService;

    }
}
