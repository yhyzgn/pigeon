package com.yhy.http.pigeon.internal;

import com.yhy.http.pigeon.provider.InterceptorProvider;
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
public class ConstructorInterceptorProvider implements InterceptorProvider {

    @Override
    public <T extends Interceptor> T provide(Class<T> interceptorClass) throws Exception {
        return interceptorClass.getConstructor().newInstance();
    }
}
