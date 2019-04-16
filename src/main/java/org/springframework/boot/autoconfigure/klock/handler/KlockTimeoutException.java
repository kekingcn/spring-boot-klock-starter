package org.springframework.boot.autoconfigure.klock.handler;

/**
 * @Author wanglaomo
 * @Date 2019/4/16
 **/
public class KlockTimeoutException extends RuntimeException {

    public KlockTimeoutException() {
    }

    public KlockTimeoutException(String message) {
        super(message);
    }

    public KlockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
