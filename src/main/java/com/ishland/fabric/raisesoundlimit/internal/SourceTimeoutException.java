package com.ishland.fabric.raisesoundlimit.internal;

public class SourceTimeoutException extends RuntimeException {

    public SourceTimeoutException() {
    }

    public SourceTimeoutException(String message) {
        super(message);
    }

    public SourceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceTimeoutException(Throwable cause) {
        super(cause);
    }

    public SourceTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
