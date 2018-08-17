package com.chl.excel.exception;

/**
 * @author lch
 * @since 2018-08-16
 */
public class DateParseErrorException extends RuntimeException {

    public DateParseErrorException() {
    }

    public DateParseErrorException(String message) {
        super(message);
    }

    public DateParseErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DateParseErrorException(Throwable cause) {
        super(cause);
    }

    public DateParseErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
