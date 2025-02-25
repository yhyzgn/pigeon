package com.yhy.http.pigeon.spring.starter.annotation;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Interceptor;
import com.yhy.http.pigeon.common.https.TrustAllCerts;
import com.yhy.http.pigeon.common.https.TrustAllHost;
import com.yhy.http.pigeon.internal.ssl.VoidSSLSocketFactory;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * 标识一个 http 客户端代理类
 * <p>
 * Created on 2021-05-22 16:33
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface Pigeon {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String qualifier() default "";

    String baseURL() default "";

    Header[] header() default {};

    Interceptor[] interceptor() default {};

    String timeout() default "6000";

    String logging() default "true";

    boolean primary() default true;

    String shouldHeaderDelegate() default "true";

    String shouldInterceptorDelegate() default "true";

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default TrustAllCerts.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default TrustAllHost.class;
}
