package com.retail.storeback.utils.exceptions;

public class InvalidInfoProductException extends RuntimeException {
    public InvalidInfoProductException() {
    }

    public InvalidInfoProductException(String message) {
        super(message);
    }

    public InvalidInfoProductException(Throwable cause) {
        super(cause);
    }

    public InvalidInfoProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInfoProductException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
