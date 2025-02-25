package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 11:55
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Headers {

    /**
     * Headers to be added to the request.
     *
     * @return Headers to be added to the request.
     */
    Header[] value() default {};
}
