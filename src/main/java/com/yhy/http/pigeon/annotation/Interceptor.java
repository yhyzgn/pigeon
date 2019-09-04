package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 17:10
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Interceptors.class)
public @interface Interceptor {

    Class<? extends okhttp3.Interceptor> value();

    boolean net() default false;
}
