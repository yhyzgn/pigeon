package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-03 12:41 <br>
 * version: 1.0.0 <br>
 * desc   : URL 参数 <br>
 * <p>
 * - @Get("/api/get") <br>
 * - Map&lt;String, Object&gt; get(@Query String name, int age, @Query("remark") String ext); <br>
 * <p>
 * - @Get("/api/get") <br>
 * - Map&lt;String, Object&gt; get(@Header Map&lt;String, Object&gt; header, @Query Map&lt;String, Object&gt; params); <br>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Query {

    String value() default "";

    boolean encoded() default false;

    String defaultValue() default "";
}
