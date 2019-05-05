package org.springframework.boot.autoconfigure.klock.handler.lock;

import org.aspectj.lang.JoinPoint;
import org.springframework.boot.autoconfigure.klock.lock.Lock;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

/**
 * 获取锁超时的处理逻辑接口
 *
 * @author wanglaomo
 * @since 2019/4/15
 **/
public interface LockTimeoutHandler {

    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
