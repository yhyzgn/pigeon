package com.yhy.http.pigeon.spring.delegate;

import com.yhy.http.pigeon.delegate.InterceptorDelegate;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * 拦截器 Spring 注入 bean
 * <p>
 * Created on 2021-11-03 10:51
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class SpringInterceptorDelegate implements InterceptorDelegate {
    private final ApplicationContext context;

    @Override
    public <T extends Interceptor> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
