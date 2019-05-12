package com.myframe.aviator.test;

import com.myframe.excel.annotation.Excel;
import com.myframe.excel.annotation.ExcelColumn;
import com.myframe.excel.constant.VersionConstant;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author LCH
 * @since 2018-06-27
 */
@Excel(value = "测试",version = VersionConstant.EXCEL_2007_ADV)
public class Demo extends SuperDemo{



    @ExcelColumn(columnName = "姓名")
    String name;

    @ExcelColumn(columnName = "编号",order = 2)
    String id;


    @ExcelColumn(columnName = "创建时间",order = 3)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date createTime;

    @ExcelColumn(columnName = "状态",formatter = StatusDataFormatter.class)
    String status;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
