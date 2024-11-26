package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-03 12:42 <br>
 * version: 1.0.0 <br>
 * desc   : Path 参数 <br>
 * <p>
 * - @Get("/api/get/path/{id}/{count}") <br>
 * - Map&lt;String, Object&gt; path(@Path("id") String alias, @Path int count, String remark); <br>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Path {

    String value() default "";

    boolean encoded() default false;

    String defaultValue() default "";
}
