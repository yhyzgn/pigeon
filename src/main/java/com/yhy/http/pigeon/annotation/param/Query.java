package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 12:41
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Query {

    String value() default "";

    boolean encoded() default false;
}
