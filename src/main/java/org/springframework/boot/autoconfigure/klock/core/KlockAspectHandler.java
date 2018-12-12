package org.springframework.boot.autoconfigure.klock.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.lock.Lock;
import org.springframework.boot.autoconfigure.klock.lock.LockFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by kl on 2017/12/29.
 * Content :给添加@KLock切面加锁处理
 */
@Aspect
@Component
@Order(0)
public class KlockAspectHandler {

    @Autowired
    LockFactory lockFactory;

    private ThreadLocal<Lock> currentThreadLock = new ThreadLocal<>();
    private ThreadLocal<Boolean> currentThreadLockRes = new ThreadLocal<>();

    @Around(value = "@annotation(klock)")
    public Object around(ProceedingJoinPoint joinPoint, Klock klock) throws Throwable {
        currentThreadLockRes.set(false);
        Lock lock = lockFactory.getLock(joinPoint,klock);
        boolean lockRes = lock.acquire();
        currentThreadLock.set(lock);
        currentThreadLockRes.set(lockRes);
        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(klock)")
    public void afterReturning(Klock klock) {
        if (currentThreadLockRes.get()) {
            currentThreadLock.get().release();
        }
    }

    @AfterThrowing(value = "@annotation(klock)")
    public void afterThrowing (Klock klock) {
        if (currentThreadLockRes.get()) {
            currentThreadLock.get().release();
        }
    }
}
