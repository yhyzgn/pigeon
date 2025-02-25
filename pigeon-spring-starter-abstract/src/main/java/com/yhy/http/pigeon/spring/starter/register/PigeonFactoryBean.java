package com.yhy.http.pigeon.spring.starter.register;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.spring.converter.SpringConverter;
import com.yhy.http.pigeon.spring.delegate.SpringHeaderDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringMethodAnnotationDelegate;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Created on 2021-11-03 11:12
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class PigeonFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;
    private SpringConverter springConverter;

    @Setter
    private Class<? extends Annotation> pigeonAnnotation;
    @Setter
    private Class<?> pigeonInterface;
    @Setter
    private String baseURL;
    @Setter
    private Map<String, String> header;
    @Setter
    private List<Class<? extends Interceptor>> interceptors;
    @Setter
    private List<Class<? extends Interceptor>> netInterceptors;
    @Setter
    private long timeout;
    @Setter
    private boolean logging;
    @Setter
    private boolean shouldHeaderDelegate;
    @Setter
    private boolean shouldInterceptorDelegate;
    @Setter
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    @Setter
    private Class<? extends X509TrustManager> sslTrustManager;
    @Setter
    private Class<? extends HostnameVerifier> sslHostnameVerifier;
    @Setter
    private List<Class<? extends Header.Dynamic>> globalHeaderList;
    @Setter
    private List<Class<? extends Interceptor>> globalInterceptorList;
    @Setter
    private List<Class<? extends Interceptor>> globalNetInterceptorList;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
        this.springConverter = new SpringConverter(context.getEnvironment());
    }

    @Override
    public Object getObject() {
        return getTarget();
    }

    @Override
    public Class<?> getObjectType() {
        return pigeonInterface;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(pigeonAnnotation, "The returned value of enableAnnotation() can not be null");
        String annotationClassName = pigeonAnnotation.getSimpleName();
        Assert.hasText(baseURL, "@" + annotationClassName + " [baseURL] can not be empty or null.");
        log.info("@{} properties for [{}] set complete.", annotationClassName, pigeonInterface);
    }

    @SuppressWarnings("unchecked")
    <T> T getTarget() {
        Pigeon.Builder builder = new Pigeon.Builder()
                .baseURL(baseURL)
                .logging(logging)
                .addConverterFactory(springConverter)
                .methodReuseEnabled(false);

        SpringHeaderDelegate headerDelegate = context.getBean(SpringHeaderDelegate.class);
        SpringInterceptorDelegate interceptorDelegate = context.getBean(SpringInterceptorDelegate.class);
        SpringMethodAnnotationDelegate methodAnnotationDelegate = context.getBean(SpringMethodAnnotationDelegate.class);

        if (!CollectionUtils.isEmpty(globalHeaderList)) {
            globalHeaderList.forEach(item -> {
                try {
                    builder.header(headerDelegate.apply(item));
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        if (!CollectionUtils.isEmpty(globalInterceptorList)) {
            globalInterceptorList.forEach(item -> {
                try {
                    builder.interceptor(interceptorDelegate.apply(item));
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        if (!CollectionUtils.isEmpty(globalNetInterceptorList)) {
            globalNetInterceptorList.forEach(item -> {
                try {
                    builder.netInterceptor(interceptorDelegate.apply(item));
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        if (!CollectionUtils.isEmpty(header)) {
            header.forEach(builder::header);
        }

        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(item -> builder.interceptor(getInstance(item)));
        }
        if (!CollectionUtils.isEmpty(netInterceptors)) {
            netInterceptors.forEach(item -> builder.netInterceptor(getInstance(item)));
        }
        if (timeout > 0) {
            builder.timeout(timeout);
        }
        if (sslSocketFactory != null && sslTrustManager != null && sslHostnameVerifier != null) {
            builder.https(getInstance(sslSocketFactory), getInstance(sslTrustManager), getInstance(sslHostnameVerifier));
        }

        if (shouldHeaderDelegate) {
            builder.delegate(headerDelegate);
        }
        if (shouldInterceptorDelegate) {
            builder.delegate(interceptorDelegate);
        }

        builder.delegate(methodAnnotationDelegate);

        return (T) builder.methodReuseEnabled(false).build().create(pigeonInterface);
    }

    private <B> B getInstance(Class<B> clazz) {
        try {
            return this.context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(clazz);
        }
    }
}
