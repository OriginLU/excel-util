package com.myframe.excel.entity;

public class ExcelConfiguration {

    private String excelName;

    private String version;

    private String fileSuffix;

    private boolean isCreateTitle;

    private ExcelColumnConfiguration[] configurations;


    public boolean isCreateTitle() {
        return isCreateTitle;
    }

    public void setCreateTitle(boolean createTitle) {
        isCreateTitle = createTitle;
    }

    public String getExcelName() {
        return excelName;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public void setExcelName(String excelName) {
        this.excelName = excelName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ExcelColumnConfiguration[] getConfigurations() {
        return configurations;
    }

    public void setConfigurations(ExcelColumnConfiguration[] configurations) {
        this.configurations = configurations;
    }
}
