package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 11:55
 * version: 1.0.0
 * desc   :
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Headers.class)
public @interface Header {
    String value() default "";

    String pairName() default "";

    String pairValue() default "";
}
