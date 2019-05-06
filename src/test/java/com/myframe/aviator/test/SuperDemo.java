package com.myframe.aviator.test;

import com.myframe.excel.annotation.ExcelColumn;


/**
 * @author lch
 * @since 2018-08-16
 */
public class SuperDemo {


    @ExcelColumn
    private String code;

    @ExcelColumn()
    private String time;



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

}
