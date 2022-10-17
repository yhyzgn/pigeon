package com.yhy.http.pigeon.spring.starter.annotation;

import com.yhy.http.pigeon.spring.delegate.SpringHeaderDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.pigeon.spring.starter.config.PigeonStarterAutoConfiguration;
import com.yhy.http.pigeon.spring.starter.register.PigeonAutoRegister;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * 启用 pigeon 组件
 * <p>
 * Created on 2021-05-22 16:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({PigeonAutoRegister.class, SpringHeaderDelegate.class, SpringInterceptorDelegate.class, PigeonStarterAutoConfiguration.class})
public @interface EnablePigeon {

    @AliasFor("basePackages")
    String[] value() default "";

    @AliasFor("value")
    String[] basePackages() default "";

    Class<?>[] basePackageClasses() default {};

    String baseURL() default "";

    PigeonHeader[] header() default {};

    PigeonInterceptor[] interceptor() default {};

    String timeout() default "6000";

    String logging() default "true";

    String shouldHeaderDelegate() default "true";

    String shouldInterceptorDelegate() default "true";

    Class<? extends SSLSocketFactory> sslSocketFactory() default SSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default X509TrustManager.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default HostnameVerifier.class;
}
