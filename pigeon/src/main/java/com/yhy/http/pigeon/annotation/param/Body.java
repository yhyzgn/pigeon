package com.yhy.http.pigeon.annotation.param;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-03 14:29 <br>
 * version: 1.0.0 <br>
 * desc   : POST Body 参数 <br>
 * <p>
 * - @POST <br>
 * - Map&lt;String, Object&gt; body(@Header Map&lt;String, Object&gt; header, @Body Map&lt;String, Object&gt; body); <br>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {
}
