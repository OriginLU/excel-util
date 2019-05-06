package com.myframe.excel.exception;

/**
 * @author lch
 * @since 2018-08-09
 */
public class ExcelCreateException extends RuntimeException {

    public ExcelCreateException(String message) {
        super(message);
    }

    public ExcelCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
