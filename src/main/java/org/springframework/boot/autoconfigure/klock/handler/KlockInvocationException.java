package org.springframework.boot.autoconfigure.klock.handler;

/**
 * @Author wanglaomo
 * @Date 2019/4/16
 **/
public class KlockInvocationException extends RuntimeException {

    public KlockInvocationException() {
    }

    public KlockInvocationException(String message) {
        super(message);
    }

    public KlockInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
