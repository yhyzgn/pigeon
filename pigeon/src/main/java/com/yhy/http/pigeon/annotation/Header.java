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
 * - @Get <br>
 * - String get(@Header MapMap&lt;String, ?&gt; header); <br>
 * <p>
 * - @Post <br>
 * - @Header(pairName = "Secret", pairValue = "ab12") <br>
 * - MapMap&lt;String, Object&gt; body(@Header MapMap&lt;String, Object&gt; header, @Body MapMap&lt;String, Object&gt; body); <br>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(Headers.class)
public @interface Header {

    /**
     * Header name
     *
     * @return header name
     */
    String value() default "";

    /**
     * Header pair name
     *
     * @return header pair name
     */
    String pairName() default "";

    /**
     * Header pair value
     *
     * @return header pair value
     */
    String pairValue() default "";

    /**
     * Header support dynamic value
     *
     * @return header support dynamic value
     */
    Class<? extends Dynamic> dynamic() default Dynamic.class;

    /**
     * Header support dynamic value
     * Must disable 'methodCache' of 'Pigeon' by way {@link com.yhy.http.pigeon.Pigeon.Builder#methodReuseEnabled(boolean)} while using 'Header.Dynamic'
     */
    interface Dynamic {

        /**
         * Get header pairs dynamically
         *
         * @param method method
         * @return header pairs
         */
        Map<String, String> pairs(Method method);
    }
}
