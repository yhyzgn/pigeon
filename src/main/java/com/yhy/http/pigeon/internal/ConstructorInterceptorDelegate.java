package com.yhy.http.pigeon.internal;

import com.yhy.http.pigeon.delegate.InterceptorDelegate;
import okhttp3.Interceptor;

/**
 * 反射构造函数方式的拦截器提供者
 * <p>
 * Created on 2021-04-19 15:23
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorInterceptorDelegate implements InterceptorDelegate {

    @Override
    public <T extends Interceptor> T apply(Class<T> interceptorClass) throws Exception {
        return interceptorClass.getConstructor().newInstance();
    }

    public static ConstructorInterceptorDelegate create() {
        return new ConstructorInterceptorDelegate();
    }
}
