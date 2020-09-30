package com.ishland.fabric.raisesoundlimit.internal;

public class SourceNotTimeoutException extends RuntimeException {

    public SourceNotTimeoutException() {
    }

    public SourceNotTimeoutException(String message) {
        super(message);
    }

    public SourceNotTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceNotTimeoutException(Throwable cause) {
        super(cause);
    }

    public SourceNotTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
