package com.chl.aviator.test;

import com.chl.excel.annotation.ExcelColumn;

/**
 * @author lch
 * @since 2018-08-16
 */
public class SuperDemo {

    @ExcelColumn(order = 3)
    private String status;

    @ExcelColumn
    private String code;

    @ExcelColumn(order = 1)
    private String time;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @ExcelColumn
    public String getSysTime(){

        return System.currentTimeMillis() + "";
    }
}