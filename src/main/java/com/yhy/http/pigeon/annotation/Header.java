package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-03 11:55 <br>
 * version: 1.0.0 <br>
 * desc   : Header 参数 <br>
 * <p>
 * - @GET <br>
 * - String get(@Header MapMap&lt;String, ?&gt; header); <br>
 * <p>
 * - @POST <br>
 * - @Header(pairName = "Secret", pairValue = "ab12") <br>
 * - MapMap&lt;String, Object&gt; body(@Header MapMap&lt;String, Object&gt; header, @Body MapMap&lt;String, Object&gt; body); <br>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Headers.class)
public @interface Header {
    String value() default "";

    String pairName() default "";

    String pairValue() default "";

    Class<? extends Dynamic> dynamic() default Dynamic.class;

    /**
     * Header support dynamic value
     * Must disable 'methodCache' of 'Pigeon' by way {@link com.yhy.http.pigeon.Pigeon.Builder#methodCache(boolean)} while using 'Header.Dynamic'
     */
    interface Dynamic {

        @Deprecated
        default String name() {
            return null;
        }

        @Deprecated
        default String value() {
            return null;
        }

        Map<String, String> pairs(Method method);
    }
}
