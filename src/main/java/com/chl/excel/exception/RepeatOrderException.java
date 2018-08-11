package com.chl.excel.exception;

/**
 * 顺序重复异常
 * @author LCH
 * @since 2018-06-28
 */
public class RepeatOrderException extends RuntimeException {

    public RepeatOrderException(String message) {
        super(message);
    }
}
