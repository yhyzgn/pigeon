package com.yhy.http.pigeon.spring.starter.annotation;

import okhttp3.Interceptor;

import java.lang.annotation.*;

/**
 * 自定义拦截器，当个被 @xxx 注解的代理接口内有效
 * <p>
 * Created on 2021-05-22 16:59
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PigeonInterceptor {

    /**
     * 具体的拦截器实现类
     *
     * @return 具体的拦截器实现类
     */
    Class<? extends Interceptor> value();

    /**
     * 是否用于网络拦截
     *
     * @return 是否用于网络拦截
     */
    boolean net() default true;
}
