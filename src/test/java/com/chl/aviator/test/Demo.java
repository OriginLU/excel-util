package com.chl.aviator.test;

import com.chl.excel.annotation.Excel;
import com.chl.excel.annotation.ExcelColumn;

/**
 * @author LCH
 * @since 2018-06-27
 */
@Excel("测试")
public class Demo extends SuperDemo{



    @ExcelColumn(columnTitle = "姓名",order = 1)
    String name;

    @ExcelColumn(columnTitle = "编号",order = 2)
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
