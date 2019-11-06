package org.springframework.boot.autoconfigure.klock.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.handler.KlockInvocationException;
import org.springframework.boot.autoconfigure.klock.lock.Lock;
import org.springframework.boot.autoconfigure.klock.lock.LockFactory;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String,LockRes> currentThreadLock = new ConcurrentHashMap<>();


    @Around(value = "@annotation(klock)")
    public Object around(ProceedingJoinPoint joinPoint, Klock klock) throws Throwable {
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,klock);
        String curentLock = this.getCurrentLockId(joinPoint,klock);
        currentThreadLock.put(curentLock,new LockRes(lockInfo, false));
        Lock lock = lockFactory.getLock(lockInfo);
        boolean lockRes = lock.acquire();

        //如果获取锁失败了，则进入失败的处理逻辑
        if(!lockRes) {
            if(logger.isWarnEnabled()) {
                logger.warn("Timeout while acquiring Lock({})", lockInfo.getName());
            }
            //如果自定义了获取锁失败的处理策略，则执行自定义的降级处理策略
            if(!StringUtils.isEmpty(klock.customLockTimeoutStrategy())) {

                return handleCustomLockTimeout(klock.customLockTimeoutStrategy(), joinPoint);

            } else {
                //否则执行预定义的执行策略
                //注意：如果没有指定预定义的策略，默认的策略为静默啥不做处理
                klock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
            }
        }

        currentThreadLock.get(curentLock).setLock(lock);
        currentThreadLock.get(curentLock).setRes(true);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(klock)")
    public void afterReturning(JoinPoint joinPoint, Klock klock) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,klock);
        releaseLock(klock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
    }

    @AfterThrowing(value = "@annotation(klock)", throwing = "ex")
    public void afterThrowing (JoinPoint joinPoint, Klock klock, Throwable ex) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,klock);
        releaseLock(klock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
        throw ex;
    }

    /**
     * 处理自定义加锁超时
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        // prepare invocation context
        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res = null;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new KlockInvocationException("Fail to invoke custom lock timeout handler: " + lockTimeoutHandler ,e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

    /**
     *  释放锁
     */
    private void releaseLock(Klock klock, JoinPoint joinPoint,String curentLock) throws Throwable {
        LockRes lockRes = currentThreadLock.get(curentLock);
        if (lockRes.getRes()) {
            boolean releaseRes = currentThreadLock.get(curentLock).getLock().release();
            // avoid release lock twice when exception happens below
            lockRes.setRes(false);
            if (!releaseRes) {
                handleReleaseTimeout(klock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }

    // avoid memory leak
    private void cleanUpThreadLocal(String curentLock) {
        currentThreadLock.remove(curentLock);
    }

    /**
     * 获取当前锁在map中的key
     * @param joinPoint
     * @param klock
     * @return
     */
    private String getCurrentLockId(JoinPoint joinPoint , Klock klock){
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,klock);
        String curentLock= Thread.currentThread().getId() + lockInfo.getName();
        return curentLock;
    }

    /**
     *  处理释放锁时已超时
     */
    private void handleReleaseTimeout(Klock klock, LockInfo lockInfo, JoinPoint joinPoint) throws Throwable {

        if(logger.isWarnEnabled()) {
            logger.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if(!StringUtils.isEmpty(klock.customReleaseTimeoutStrategy())) {

            handleCustomReleaseTimeout(klock.customReleaseTimeoutStrategy(), joinPoint);

        } else {
            klock.releaseTimeoutStrategy().handle(lockInfo);
        }

    }

    /**
     * 处理自定义释放锁时已超时
     */
    private void handleCustomReleaseTimeout(String releaseTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(releaseTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new KlockInvocationException("Fail to invoke custom release timeout handler: " + releaseTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private class LockRes {

        private LockInfo lockInfo;
        private Lock lock;
        private Boolean res;

        LockRes(LockInfo lockInfo, Boolean res) {
            this.lockInfo = lockInfo;
            this.res = res;
        }

        LockInfo getLockInfo() {
            return lockInfo;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
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
