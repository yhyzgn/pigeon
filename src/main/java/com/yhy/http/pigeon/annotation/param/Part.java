package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com <br/>
 * time   : 2019-09-03 12:42 <br/>
 * version: 1.0.0 <br/>
 * desc   : multipart 文件上传参数 <br/>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Part {

    String value() default "";

    String encoding() default "binary";
}
