package com.yhy.http.pigeon.spring.starter.annotation;

import java.lang.annotation.*;

/**
 * 自定义请求头，当个被 @xxx 注解的代理接口内有效
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
public @interface PigeonHeader {

    /**
     * 请求头名称
     *
     * @return 请求头名称
     */
    String name();

    /**
     * 请求头值
     *
     * @return 请求头值
     */
    String value();
}
