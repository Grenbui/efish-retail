package com.retail.storeback.utils.exceptions;

public class MinioConnectionException extends RuntimeException {
    public MinioConnectionException(String message) {
        super(message);
    }

    public MinioConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioConnectionException(Throwable cause) {
        super(cause);
    }

    public MinioConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MinioConnectionException() {
    }
}
