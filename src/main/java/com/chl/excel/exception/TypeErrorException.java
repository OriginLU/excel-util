package com.chl.excel.exception;

/**
 * @author lch
 * @since 2018-08-16
 */
public class TypeErrorException extends RuntimeException{


    public TypeErrorException() {
    }

    public TypeErrorException(String message) {
        super(message);
    }

    public TypeErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeErrorException(Throwable cause) {
        super(cause);
    }

    public TypeErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
