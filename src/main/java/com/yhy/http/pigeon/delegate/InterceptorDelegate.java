package com.yhy.http.pigeon.delegate;

/**
 * 拦截器提供者
 * <p>
 * Created on 2021-04-19 15:21
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface InterceptorDelegate {

    /**
     * 拦截器提供者
     *
     * @param interceptorClass 拦截器提供者类
     * @param <T>              拦截器类
     * @return 对象
     * @throws Exception 可能出现的异常
     */
    <T extends okhttp3.Interceptor> T apply(Class<T> interceptorClass) throws Exception;
}
