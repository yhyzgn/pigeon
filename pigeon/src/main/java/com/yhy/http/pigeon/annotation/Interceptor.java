package com.yhy.http.pigeon.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅 <br>
 * e-mail : yhyzgn@gmail.com <br>
 * time   : 2019-09-04 17:10 <br>
 * version: 1.0.0 <br>
 * desc   : 拦截器 <br>
 * <p>
 * - @Get("/api/get") <br>
 * - @Interceptor(TestInterceptor.class) <br>
 * - Map&lt;String, Object&gt; get(String name, int age); <br>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Interceptors.class)
public @interface Interceptor {

    /**
     * 拦截器类
     *
     * @return 拦截器类
     */
    Class<? extends okhttp3.Interceptor> value();

    /**
     * 是否网络请求拦截器
     *
     * @return 是否网络请求拦截器
     */
    boolean net() default true;
}
