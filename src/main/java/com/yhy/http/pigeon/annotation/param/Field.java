package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com <br/>
 * time   : 2019-09-03 12:42 <br/>
 * version: 1.0.0 <br/>
 * desc   : POST 参数 <br/>
 * <p>
 * 必须搭配 {@link com.yhy.http.pigeon.annotation.Form} 使用
 *
 * <p>
 * - @POST("/api/post") <br/>
 * - @Form <br/>
 * - Map&lt;String, Object&gt; post(@Query String name, int age, @Field("remark") String ext); <br/>
 * <p>
 * - @POST("/api/post/cat") <br/>
 * - @Form <br/>
 * - Map&lt;String, Object&gt; post(@Header Map&lt;String, Object&gt; header, @Field Map&lt;String, Object&gt; params); <br/>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Field {

    String value() default "";

    boolean encoded() default false;

    String defaultValue() default "";
}
