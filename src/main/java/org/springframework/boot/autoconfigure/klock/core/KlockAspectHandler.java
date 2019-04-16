package org.springframework.boot.autoconfigure.klock.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.handler.KlockTimeoutException;
import org.springframework.boot.autoconfigure.klock.handler.lock.AbstractReleaseTimeoutHandler;
import org.springframework.boot.autoconfigure.klock.handler.release.AbstractLockTimeoutHandler;
import org.springframework.boot.autoconfigure.klock.lock.Lock;
import org.springframework.boot.autoconfigure.klock.lock.LockFactory;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
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

    private static final Logger logger = LoggerFactory.getLogger(KlockAspectHandler.class);

    @Autowired
    LockFactory lockFactory;

    @Autowired
    private LockInfoProvider lockInfoProvider;

    private ThreadLocal<Lock> currentThreadLock = new ThreadLocal<>();
    private ThreadLocal<LockRes> currentThreadLockRes = new ThreadLocal<>();

    @Around(value = "@annotation(klock)")
    public Object around(ProceedingJoinPoint joinPoint, Klock klock) throws Throwable {
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,klock);
        currentThreadLockRes.set(new LockRes(lockInfo, false));
        Lock lock = lockFactory.getLock(lockInfo);
        boolean lockRes = lock.acquire();

        if(!lockRes) {
            handleLockTimeout(klock, lockInfo, lock, joinPoint);
        }

        currentThreadLock.set(lock);
        currentThreadLockRes.get().setRes(true);
        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(klock)")
    public void afterReturning(Klock klock) {

        releaseLock(klock);
    }

    @AfterThrowing(value = "@annotation(klock)", throwing = "ex")
    public void afterThrowing (Klock klock, Throwable ex) throws Throwable {
        releaseLock(klock);
        throw ex;
    }

    /**
     * 处理加锁超时
     */
    private void handleLockTimeout(Klock klock, LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

        if(logger.isWarnEnabled()) {
            logger.warn("Timeout while acquiring Lock({})", lockInfo.getName());
        }

        if(klock.customLockTimeout() != AbstractLockTimeoutHandler.None.class) {

            handleCustomLockTimeout(klock.customLockTimeout(), lockInfo, lock, joinPoint);

        } else {
            klock.lockTimeout().handle(lockInfo, lock, joinPoint);
        }

    }

    /**
     * 处理自定义加锁超时
     */
    private void handleCustomLockTimeout(Class<? extends AbstractLockTimeoutHandler> customLockTimeout, LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

        AbstractLockTimeoutHandler handler = null;
        try {
            handler = customLockTimeout.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeout",e);
        }
        handler.handle(lockInfo, lock, joinPoint);

    }

    /**
     *  释放锁
     */
    private void releaseLock(Klock klock) {
        LockRes lockRes = currentThreadLockRes.get();
        if (lockRes.getRes()) {
            boolean releaseRes = currentThreadLock.get().release();

            if (!releaseRes) {
                handleReleaseTimeout(klock, lockRes.getLockInfo());
            }
        }
    }


    /**
     *  处理释放锁时已超时
     */
    private void handleReleaseTimeout(Klock klock, LockInfo lockInfo) {

        if(logger.isWarnEnabled()) {
            logger.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if(klock.customReleaseTimeout() != AbstractReleaseTimeoutHandler.None.class) {

            handleCustomReleaseTimeout(klock.customReleaseTimeout(), lockInfo);

        } else {
            klock.releaseTimeout().handle(lockInfo);
        }

    }

    private void handleCustomReleaseTimeout(Class<? extends AbstractReleaseTimeoutHandler> customReleaseTimeout, LockInfo lockInfo) {

        AbstractReleaseTimeoutHandler handler = null;
        try {
            handler = customReleaseTimeout.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeout",e);
        }

        handler.handle(lockInfo);
    }

    private class LockRes {

        private LockInfo lockInfo;

        private Boolean res;

        LockRes(LockInfo lockInfo, Boolean res) {
            this.lockInfo = lockInfo;
            this.res = res;
        }

        LockInfo getLockInfo() {
            return lockInfo;
        }

        Boolean getRes() {
            return res;
        }

        void setRes(Boolean res) {
            this.res = res;
        }

        void setLockInfo(LockInfo lockInfo) {
            this.lockInfo = lockInfo;
        }
    }
}
