package com.chl.aviator.test;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;

/**
 * @author LCH
 * @since 2018-06-27
 */
@Excel
public class Demo {

    @ExcelColumn(columnTitle = "姓名")
    String name;

    @ExcelColumn(columnTitle = "编号")
    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
