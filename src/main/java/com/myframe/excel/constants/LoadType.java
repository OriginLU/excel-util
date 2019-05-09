package com.myframe.excel.constants;

public enum  LoadType {

    IMPORT(0,"导入"),
    EXPORT(1,"导出");


    LoadType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private int type;


    private String desc;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
