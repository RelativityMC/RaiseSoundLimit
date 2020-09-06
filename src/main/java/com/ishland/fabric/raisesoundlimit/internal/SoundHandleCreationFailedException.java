package com.ishland.fabric.raisesoundlimit.internal;

public class SoundHandleCreationFailedException extends RuntimeException {
    public SoundHandleCreationFailedException() {
    }

    public SoundHandleCreationFailedException(String message) {
        super(message);
    }

    public SoundHandleCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoundHandleCreationFailedException(Throwable cause) {
        super(cause);
    }

    public SoundHandleCreationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
