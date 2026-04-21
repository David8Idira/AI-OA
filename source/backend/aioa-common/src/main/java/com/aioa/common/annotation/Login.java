package com.aioa.common.annotation;

import java.lang.annotation.*;

/**
 * Login required annotation
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {
}
