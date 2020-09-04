package org.springframework.boot.autoconfigure.klock.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.config.KlockConfig;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.klock.model.LockType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kl on 2017/12/29.
 */
public class LockInfoProvider {

    private static final String LOCK_NAME_PREFIX = "lock";
    private static final String LOCK_NAME_SEPARATOR = ".";


    @Autowired
    private KlockConfig klockConfig;

    @Autowired
    private BusinessKeyProvider businessKeyProvider;

    private static final Logger logger = LoggerFactory.getLogger(LockInfoProvider.class);

    List<LockInfo> get(JoinPoint joinPoint, Klock klock) {
        List<LockInfo> lockInfos = new ArrayList<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockType type = klock.lockType();
        Method method = businessKeyProvider.getMethod(joinPoint);
        List<String> parameterKeys = businessKeyProvider.getParameterKey(method.getParameters(), joinPoint.getArgs());

        long waitTime = getWaitTime(klock);
        long leaseTime = getLeaseTime(klock);

        if (parameterKeys.size() > 0) {
            parameterKeys.forEach(parameterKey -> {
                String businessKeyName = businessKeyProvider.getKeyName(joinPoint, klock, parameterKey);
                //锁的名字，锁的粒度就是这里控制的
                addLockInfo(klock, lockInfos, signature, type, waitTime, leaseTime, businessKeyName);
            });
        } else {
            String businessKeyName = businessKeyProvider.getKeyName(joinPoint, klock, null);
            //锁的名字，锁的粒度就是这里控制的
            addLockInfo(klock, lockInfos, signature, type, waitTime, leaseTime, businessKeyName);
        }

        return lockInfos;
    }

    private void addLockInfo(Klock klock, List<LockInfo> lockInfos, MethodSignature signature, LockType type, long waitTime, long leaseTime, String businessKeyName) {
        String lockName = LOCK_NAME_PREFIX + LOCK_NAME_SEPARATOR + getName(klock.name(), signature) + businessKeyName;
        if (leaseTime == -1 && logger.isWarnEnabled()) {
            logger.warn("Trying to acquire Lock({}) with no expiration, " +
                    "Klock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                    "This may cause dead lock in some circumstances.", lockName);
        }
        lockInfos.add(new LockInfo(type, lockName, waitTime, leaseTime));
    }

    /**
     * 获取锁的name，如果没有指定，则按全类名拼接方法名处理
     *
     * @param annotationName
     * @param signature
     * @return
     */
    private String getName(String annotationName, MethodSignature signature) {
        if (annotationName.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return annotationName;
        }
    }


    private long getWaitTime(Klock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
                klockConfig.getWaitTime() : lock.waitTime();
    }

    private long getLeaseTime(Klock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
                klockConfig.getLeaseTime() : lock.leaseTime();
    }
}
