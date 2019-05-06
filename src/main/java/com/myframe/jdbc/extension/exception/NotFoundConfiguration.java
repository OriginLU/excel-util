package com.myframe.jdbc.extension.exception;

/**
 * @author lch
 * @since 2019-03-23
 */
public class NotFoundConfiguration extends RuntimeException {


    public NotFoundConfiguration() {
    }

    public NotFoundConfiguration(String message) {
        super(message);
    }

    public NotFoundConfiguration(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundConfiguration(Throwable cause) {
        super(cause);
    }

    public NotFoundConfiguration(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
