package com.yhy.http.pigeon.annotation.method;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-02 15:52 <br>
 * version: 1.0.0 <br>
 * desc   : options请求 <br>
 * <p>
 * - @OPTIONS("/api/options") <br>
 * - String options(); <br>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OPTIONS {

    /**
     * api子路径
     *
     * @return api子路径
     */
    String value() default "";
}
