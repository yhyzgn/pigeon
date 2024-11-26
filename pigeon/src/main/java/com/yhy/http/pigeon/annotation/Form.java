package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-03 14:29 <br>
 * version: 1.0.0 <br>
 * desc   : Post 方法中指定为 form-data <br>
 * <p>
 * 用于搭配 {@link com.yhy.http.pigeon.annotation.param.Field}
 *
 * <p>
 * - @Post("/api/post") <br>
 * - @Form <br>
 * - Map&lt;String, Object&gt; post(@Query String name, int age, @Field("remark") String ext); <br>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Form {
}
