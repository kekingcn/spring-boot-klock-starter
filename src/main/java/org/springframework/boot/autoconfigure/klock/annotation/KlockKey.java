package org.springframework.boot.autoconfigure.klock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kl on 2018/1/24.
 * Content :
 */
@Target(value = {ElementType.PARAMETER, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface KlockKey {
    String value() default "";
}
