package org.springframework.boot.autoconfigure.klock.handler.release;

import org.aspectj.lang.JoinPoint;
import org.springframework.boot.autoconfigure.klock.handler.lock.LockTimeoutHandler;
import org.springframework.boot.autoconfigure.klock.lock.Lock;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

/**
 * @Author wanglaomo
 * @Date 2019/4/16
 **/
public abstract class AbstractLockTimeoutHandler implements LockTimeoutHandler {

    public AbstractLockTimeoutHandler() {

    }

    public static class None extends AbstractLockTimeoutHandler {

        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

        }
    }
}
