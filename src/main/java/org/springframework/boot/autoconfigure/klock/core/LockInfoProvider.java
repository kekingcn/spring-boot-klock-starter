package org.springframework.boot.autoconfigure.klock.core;

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.config.KlockConfig;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.klock.model.LockType;

/**
 * Created by kl on 2017/12/29.
 */
public class LockInfoProvider {

    public static final String LOCK_NAME_PREFIX = "lock";
    public static final String LOCK_NAME_SEPARATOR = ".";

    @Autowired
    private KlockConfig klockConfig;

    public LockInfo get(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = getMethod(joinPoint, signature);
        Klock annotation = method.getAnnotation(Klock.class);
        LockType type= annotation.lockType();
        String name = LOCK_NAME_PREFIX+LOCK_NAME_SEPARATOR+getName(annotation.name(), signature);
        long waitTime = getWaitTime(annotation);
        long leaseTime = getLeaseTime(annotation);
        return new LockInfo(type,name,waitTime,leaseTime);
    }

    private Method getMethod(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        Method method = signature.getMethod();

        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (SecurityException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to get target method");
            }
        }

        return method;
    }


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
