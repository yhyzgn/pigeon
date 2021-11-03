package com.yhy.http.pigeon.spring.starter.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.internal.converter.JacksonConverter;
import com.yhy.http.pigeon.internal.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.internal.ssl.VoidSSLSocketFactory;
import com.yhy.http.pigeon.internal.ssl.VoidSSLX509TrustManager;
import com.yhy.http.pigeon.spring.delegate.SpringHeaderDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringInterceptorDelegate;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PigeonFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(PigeonFactoryBean.class);

    private ApplicationContext context;

    private Class<? extends Annotation> pigeonAnnotation;
    private Class<?> pigeonInterface;
    private String baseURL;
    private Map<String, String> header;
    private List<Class<? extends Interceptor>> interceptors;
    private List<Class<? extends Interceptor>> netInterceptors;
    private long timeout;
    private boolean logging;
    private boolean shouldHeaderDelegate;
    private boolean shouldInterceptorDelegate;
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    private Class<? extends X509TrustManager> sslTrustManager;
    private Class<? extends HostnameVerifier> sslHostnameVerifier;
    private JacksonConverter defaultConverter;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public Object getObject() throws Exception {
        return getTarget();
    }

    @Override
    public Class<?> getObjectType() {
        return pigeonInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(pigeonAnnotation, "PigeonConfig.annotationClass can not be null");
        String annotationClassName = pigeonAnnotation.getSimpleName();
        Assert.hasText(baseURL, "@" + annotationClassName + " [baseURL] can not be empty or null.");
        LOGGER.info("@" + annotationClassName + " properties for [{}] set complete.", pigeonInterface);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        defaultConverter = new JacksonConverter(objectMapper);
    }

    @SuppressWarnings("unchecked")
    <T> T getTarget() {
        Pigeon.Builder builder = new Pigeon.Builder().baseURL(baseURL).logging(logging);
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
        if (sslSocketFactory != null && sslSocketFactory != VoidSSLSocketFactory.class && sslTrustManager != null && sslTrustManager != VoidSSLX509TrustManager.class && sslHostnameVerifier != null && sslHostnameVerifier != VoidSSLHostnameVerifier.class) {
            builder.https(getInstance(sslSocketFactory), getInstance(sslTrustManager), getInstance(sslHostnameVerifier));
        }

        builder.addConverterFactory(defaultConverter);

        if (shouldHeaderDelegate) {
            builder.delegate(context.getBean(SpringHeaderDelegate.class));
        }
        if (shouldInterceptorDelegate) {
            builder.delegate(context.getBean(SpringInterceptorDelegate.class));
        }

        return (T) builder.methodReuseEnabled(false).build().create(pigeonInterface);
    }

    private <B> B getInstance(Class<B> clazz) {
        try {
            return this.context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(clazz);
        }
    }

    public void setPigeonAnnotation(Class<? extends Annotation> pigeonAnnotation) {
        this.pigeonAnnotation = pigeonAnnotation;
    }

    public void setPigeonInterface(Class<?> pigeonInterface) {
        this.pigeonInterface = pigeonInterface;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void setInterceptors(List<Class<? extends Interceptor>> interceptors) {
        this.interceptors = interceptors;
    }

    public void setNetInterceptors(List<Class<? extends Interceptor>> netInterceptors) {
        this.netInterceptors = netInterceptors;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public void setShouldHeaderDelegate(boolean shouldHeaderDelegate) {
        this.shouldHeaderDelegate = shouldHeaderDelegate;
    }

    public void setShouldInterceptorDelegate(boolean shouldInterceptorDelegate) {
        this.shouldInterceptorDelegate = shouldInterceptorDelegate;
    }

    public void setSslSocketFactory(Class<? extends SSLSocketFactory> sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public void setSslTrustManager(Class<? extends X509TrustManager> sslTrustManager) {
        this.sslTrustManager = sslTrustManager;
    }

    public void setSslHostnameVerifier(Class<? extends HostnameVerifier> sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
    }

    public void setDefaultConverter(JacksonConverter defaultConverter) {
        this.defaultConverter = defaultConverter;
    }
}
