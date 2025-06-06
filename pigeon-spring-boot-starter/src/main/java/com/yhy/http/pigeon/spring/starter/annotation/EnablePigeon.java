package com.yhy.http.pigeon.spring.starter.annotation;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Interceptor;
import com.yhy.http.pigeon.common.https.TrustAllCerts;
import com.yhy.http.pigeon.common.https.TrustAllHost;
import com.yhy.http.pigeon.internal.ssl.VoidSSLSocketFactory;
import com.yhy.http.pigeon.spring.delegate.SpringHeaderDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.pigeon.spring.delegate.SpringMethodAnnotationDelegate;
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
@Import({PigeonAutoRegister.class, PigeonStarterAutoConfiguration.class, SpringHeaderDelegate.class, SpringInterceptorDelegate.class, SpringMethodAnnotationDelegate.class})
public @interface EnablePigeon {

    @AliasFor("basePackages")
    String[] value() default "";

    @AliasFor("value")
    String[] basePackages() default "";

    Class<?>[] basePackageClasses() default {};

    String baseURL() default "";

    Header[] header() default {};

    Interceptor[] interceptor() default {};

    String timeout() default "6000";

    String logging() default "true";

    String shouldHeaderDelegate() default "true";

    String shouldInterceptorDelegate() default "true";

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default TrustAllCerts.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default TrustAllHost.class;
}
