package com.yhy.http.pigeon.spring.starter.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created on 2022-10-17 23:32
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class PigeonStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SSLSocketFactory sslSocketFactory(TrustManager manager) {
        SSLSocketFactory socketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{manager}, new SecureRandom());
            socketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("", e);
        }
        return socketFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    @Bean
    @ConditionalOnMissingBean
    public Dispatcher dispatcher() {
        return new Dispatcher(pigeonVirtualExecutor());
    }

    private ExecutorService pigeonVirtualExecutor() {
        ThreadFactory factory = Thread.ofVirtual().name("Pigeon-VT-#", 1).factory();
        ExecutorService virtualExecutor = Executors.newThreadPerTaskExecutor(factory);
        return TtlExecutors.getTtlExecutorService(virtualExecutor);
    }
}
