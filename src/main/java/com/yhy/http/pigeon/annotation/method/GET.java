package com.yhy.http.pigeon.annotation.method;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 15:50
 * version: 1.0.0
 * desc   : get请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GET {

    /**
     * api子路径
     *
     * @return api子路径
     */
    String value() default "";
}
