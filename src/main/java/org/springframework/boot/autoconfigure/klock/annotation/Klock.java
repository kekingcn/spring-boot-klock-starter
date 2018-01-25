package org.springframework.boot.autoconfigure.klock.annotation;

import org.springframework.boot.autoconfigure.klock.model.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kl on 2017/12/29.
 * Content :加锁注解
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Klock {
    /**
     * 锁的名称
     * @return
     */
    String name() default "";
    /**
     * 锁类型，默认可重入锁
     * @return
     */
    LockType lockType() default LockType.Reentrant;
    /**
     * 尝试加锁，最多等待时间
     * @return
     */
    long waitTime() default Long.MIN_VALUE;
    /**
     *上锁以后xxx秒自动解锁
     * @return
     */
    long leaseTime() default Long.MIN_VALUE;

    /**
     * 自定义业务key
     * @return
     */
     String [] keys() default {};
}
