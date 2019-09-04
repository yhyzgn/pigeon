package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 17:11
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptors {

    Interceptor[] value() default {};
}
