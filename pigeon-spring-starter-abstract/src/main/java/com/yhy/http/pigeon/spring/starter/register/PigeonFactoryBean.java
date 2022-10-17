package com.yhy.http.pigeon.spring.starter.register;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.spring.converter.SpringConverter;
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

    private SpringConverter springConverter;

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

    private List<Class<? extends Header.Dynamic>> globalHeaderList;
    private List<Class<? extends Interceptor>> globalInterceptorList;
    private List<Class<? extends Interceptor>> globalNetInterceptorList;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
        this.springConverter = new SpringConverter(context.getEnvironment());
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
        Assert.notNull(pigeonAnnotation, "The returned value of enableAnnotation() can not be null");
        String annotationClassName = pigeonAnnotation.getSimpleName();
        Assert.hasText(baseURL, "@" + annotationClassName + " [baseURL] can not be empty or null.");
        LOGGER.info("@" + annotationClassName + " properties for [{}] set complete.", pigeonInterface);
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

        if (!CollectionUtils.isEmpty(globalHeaderList)) {
            globalHeaderList.forEach(item -> {
                try {
                    builder.header(headerDelegate.apply(item));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (!CollectionUtils.isEmpty(globalInterceptorList)) {
            globalInterceptorList.forEach(item -> {
                try {
                    builder.interceptor(interceptorDelegate.apply(item));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (!CollectionUtils.isEmpty(globalNetInterceptorList)) {
            globalNetInterceptorList.forEach(item -> {
                try {
                    builder.netInterceptor(interceptorDelegate.apply(item));
                } catch (Exception e) {
                    e.printStackTrace();
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

    public void setGlobalHeaderList(List<Class<? extends Header.Dynamic>> globalHeaderList) {
        this.globalHeaderList = globalHeaderList;
    }

    public void setGlobalInterceptorList(List<Class<? extends Interceptor>> globalInterceptorList) {
        this.globalInterceptorList = globalInterceptorList;
    }

    public void setGlobalNetInterceptorList(List<Class<? extends Interceptor>> globalNetInterceptorList) {
        this.globalNetInterceptorList = globalNetInterceptorList;
    }
}
