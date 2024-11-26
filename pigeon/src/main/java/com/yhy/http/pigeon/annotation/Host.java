package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 14:29
 * version: 1.0.0
 * desc   :
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Host {

    /**
     * Host 地址
     *
     * @return Host 地址
     */
    String value() default "";
}
