package com.yhy.http.pigeon.spring.delegate;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.delegate.HeaderDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * 请求头 Spring 注入 bean
 * <p>
 * Created on 2021-11-03 10:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class SpringHeaderDelegate implements HeaderDelegate {
    private final ApplicationContext context;

    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
