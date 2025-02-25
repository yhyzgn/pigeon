package com.yhy.http.pigeon;

import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.delegate.HeaderDelegate;
import com.yhy.http.pigeon.delegate.InterceptorDelegate;
import com.yhy.http.pigeon.delegate.MethodAnnotationDelegate;
import com.yhy.http.pigeon.http.HttpMethod;
import com.yhy.http.pigeon.internal.adapter.GuavaCallAdapter;
import com.yhy.http.pigeon.internal.converter.JacksonConverter;
import com.yhy.http.pigeon.internal.logging.HttpLoggerInterceptor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 12:34
 * version: 1.0.0
 * desc   : Pigeon
 */
@SuppressWarnings("unchecked")
public class Pigeon {
    private final Map<Method, HttpMethod<?>> httpMethodMap = new ConcurrentHashMap<>();

    private final HttpUrl host;
    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager sslTrustManager;
    private final HostnameVerifier sslHostnameVerifier;
    private final List<Interceptor> netInterceptors;
    private final List<Interceptor> interceptors;
    private final Map<String, String> headers;
    private final List<Header.Dynamic> dynamicHeaders;
    private final List<CallAdapter.Factory> callFactories;
    private final List<Converter.Factory> converterFactories;
    private final HeaderDelegate headerDelegate;
    private final InterceptorDelegate interceptorDelegate;
    private final MethodAnnotationDelegate methodAnnotationDelegate;
    private final OkHttpClient.Builder client;
    private final boolean methodReuseEnabled;

    private Pigeon(Builder builder) {
        this.host = builder.host;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.sslTrustManager = builder.sslTrustManager;
        this.sslHostnameVerifier = builder.sslHostnameVerifier;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.dynamicHeaders = builder.dynamicHeaders;
        this.callFactories = builder.adapterFactories;
        this.converterFactories = builder.converterFactories;
        this.headerDelegate = builder.headerDelegate;
        this.interceptorDelegate = builder.interceptorDelegate;
        this.methodAnnotationDelegate = builder.methodAnnotationDelegate;
        this.client = builder.client;
        this.methodReuseEnabled = builder.methodReuseEnabled;
    }

    @NotNull
    public HttpUrl host() {
        return host;
    }

    @NotNull
    public List<Interceptor> netInterceptors() {
        return netInterceptors;
    }

    @NotNull
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    @NotNull
    public Map<String, String> headers() {
        return headers;
    }

    @NotNull
    public List<Header.Dynamic> dynamicHeaders() {
        return dynamicHeaders;
    }

    public HeaderDelegate headerDelegate() {
        return headerDelegate;
    }

    public InterceptorDelegate interceptorDelegate() {
        return interceptorDelegate;
    }

    public MethodAnnotationDelegate methodAnnotationDelegate() {
        return methodAnnotationDelegate;
    }

    public CallAdapter<?, ?> adapter(Type returnType, Annotation[] annotations) {
        return findCallAdapter(returnType, annotations);
    }

    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        return findStringConverter(type, annotations);
    }

    public <T> Converter<T, RequestBody> requestConverter(Type type, Annotation[] parameterAnnotations) {
        return findRequestConverter(type, parameterAnnotations);
    }

    public <T> Converter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        return findResponseConverter(responseType, annotations);
    }

    public OkHttpClient.Builder client() {
        // 返回干净的builder，‘client’中只包含基础的 OkHttpClient 配置
        // 兼容低版本的 OkHttpClient.Builder
        return newBuilder();
    }

    public <T> T create(Class<T> api) {
        Objects.requireNonNull(api, "api can not be null.");
        validateInterface(api);
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return loadHttpMethod(method).invoke(null != args ? args : new Object[0]);
        });
    }

    private <T> Converter<T, RequestBody> findRequestConverter(Type type, Annotation[] annotations) {
        Converter<T, RequestBody> converter;
        // 从后向前查找
        for (int i = converterFactories.size() - 1; i >= 0; i--) {
            converter = (Converter<T, RequestBody>) converterFactories.get(i).requestBodyConverter(type, annotations, this);
            if (null != converter) {
                return converter;
            }
        }
        throw new IllegalStateException("Can not found adapted RequestConverter.");
    }

    private <T> Converter<ResponseBody, T> findResponseConverter(Type type, Annotation[] annotations) {
        Converter<ResponseBody, T> converter;
        // 从后向前查找
        for (int i = converterFactories.size() - 1; i >= 0; i--) {
            converter = (Converter<ResponseBody, T>) converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (null != converter) {
                return converter;
            }
        }
        throw new IllegalStateException("Can not found adapted ResponseConverter.");
    }

    private <T> Converter<T, String> findStringConverter(Type type, Annotation[] annotations) {
        Converter<T, String> converter;
        // 从后向前查找
        for (int i = converterFactories.size() - 1; i >= 0; i--) {
            converter = (Converter<T, String>) converterFactories.get(i).stringConverter(type, annotations, this);
            if (null != converter) {
                return converter;
            }
        }
        throw new IllegalStateException("Can not found adapted StringConverter.");
    }

    private CallAdapter<?, ?> findCallAdapter(Type returnType, Annotation[] annotations) {
        CallAdapter<?, ?> adapter;
        // 从后向前查找
        for (int i = callFactories.size() - 1; i >= 0; i--) {
            adapter = callFactories.get(i).get(returnType, annotations, this);
            if (null != adapter) {
                return adapter;
            }
        }
        throw new IllegalStateException("Can not found adapted CallAdapter.");
    }

    private void validateInterface(Class<?> api) {
        if (!api.isInterface()) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] must be interface.");
        }
        if (api.getTypeParameters().length != 0) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] can not contains any typeParameter.");
        }
        for (Method method : api.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                loadHttpMethod(method);
            }
        }
    }

    private HttpMethod<?> loadHttpMethod(Method method) {
        if (!methodReuseEnabled) {
            return HttpMethod.parseAnnotations(this, method);
        }

        // 启用了代理缓存
        HttpMethod<?> result = httpMethodMap.get(method);
        if (null != result) return result;
        synchronized (httpMethodMap) {
            result = httpMethodMap.get(method);
            if (null == result) {
                result = HttpMethod.parseAnnotations(this, method);
                httpMethodMap.put(method, result);
            }
            return result;
        }
    }

    private OkHttpClient.Builder newBuilder() {
        OkHttpClient ok = client.build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(ok.dispatcher())
                .connectionPool(ok.connectionPool())
                .eventListenerFactory(ok.eventListenerFactory())
                .retryOnConnectionFailure(ok.retryOnConnectionFailure())
                .authenticator(ok.authenticator())
                .followRedirects(ok.followRedirects())
                .followSslRedirects(ok.followSslRedirects())
                .cookieJar(ok.cookieJar())
                .cache(ok.cache())
                .dns(ok.dns())
                .proxy(ok.proxy())
                .proxySelector(ok.proxySelector())
                .proxyAuthenticator(ok.proxyAuthenticator())
                .socketFactory(ok.socketFactory())
                .connectionSpecs(ok.connectionSpecs())
                .protocols(ok.protocols())
                .hostnameVerifier(ok.hostnameVerifier())
                .certificatePinner(ok.certificatePinner())
                .callTimeout(Duration.ofMillis(ok.callTimeoutMillis()))
                .connectTimeout(Duration.ofMillis(ok.connectTimeoutMillis()))
                .readTimeout(Duration.ofMillis(ok.readTimeoutMillis()))
                .writeTimeout(Duration.ofMillis(ok.writeTimeoutMillis()))
                .pingInterval(Duration.ofMillis(ok.pingIntervalMillis()))
                .certificatePinner(ok.certificatePinner());

        // 配置 ssl
        if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
            builder.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
        }

        return builder;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private HttpUrl host;
        private final List<Interceptor> netInterceptors = new ArrayList<>();
        private final List<Interceptor> interceptors = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();
        private final List<Header.Dynamic> dynamicHeaders = new ArrayList<>();
        private final List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private HeaderDelegate headerDelegate;
        private InterceptorDelegate interceptorDelegate;
        private MethodAnnotationDelegate methodAnnotationDelegate;
        private OkHttpClient.Builder client;
        private boolean logging = true;
        private boolean methodReuseEnabled = true;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager sslTrustManager;
        private HostnameVerifier sslHostnameVerifier;
        private Duration timeout;

        public Builder baseURL(String url) {
            return this.host(url);
        }

        public Builder host(String url) {
            Objects.requireNonNull(url, "URL can not be null.");
            this.host = HttpUrl.get(url);
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder netInterceptor(Interceptor interceptor) {
            this.netInterceptors.add(interceptor);
            return this;
        }

        /**
         * 静态请求头
         *
         * @param name  名称
         * @param value 静态值
         * @return builder
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * 动态请求头
         *
         * @param header 动态支持
         * @return builder
         */
        public Builder header(Header.Dynamic header) {
            this.dynamicHeaders.add(header);
            return this;
        }

        /**
         * 请求头提供者
         *
         * @param delegate 提供者
         * @return builder
         */
        public Builder delegate(HeaderDelegate delegate) {
            this.headerDelegate = delegate;
            return this;
        }

        /**
         * 拦截器提供者
         *
         * @param delegate 提供者
         * @return builder
         */
        public Builder delegate(InterceptorDelegate delegate) {
            this.interceptorDelegate = delegate;
            return this;
        }

        /**
         * 方法注解解析器
         *
         * @param delegate 解析器
         * @return builder
         */
        public Builder delegate(MethodAnnotationDelegate delegate) {
            this.methodAnnotationDelegate = delegate;
            return this;
        }

        public Builder addAdapterFactory(CallAdapter.Factory factory) {
            this.adapterFactories.add(factory);
            return this;
        }

        public Builder addConverterFactory(Converter.Factory factory) {
            this.converterFactories.add(factory);
            return this;
        }

        public Builder https(SSLSocketFactory factory, X509TrustManager manager, HostnameVerifier verifier) {
            this.sslSocketFactory = factory;
            this.sslTrustManager = manager;
            this.sslHostnameVerifier = verifier;
            return this;
        }

        public Builder client(OkHttpClient.Builder client) {
            this.client = client;
            return this;
        }

        public Builder timeout(long millis) {
            this.timeout = Duration.ofMillis(millis);
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder logging(boolean enabled) {
            this.logging = enabled;
            return this;
        }

        public Builder methodReuseEnabled(boolean enabled) {
            this.methodReuseEnabled = enabled;
            return this;
        }

        public Pigeon build() {
            if (null == host) {
                throw new IllegalStateException("host can not be null.");
            }

            // 默认 Adapter 和 Converter 需要添加在最前面，后边需要从后往前查找
            adapterFactories.addFirst(new GuavaCallAdapter());
            converterFactories.addFirst(new JacksonConverter());

            if (null == client) {
                client = new OkHttpClient.Builder();
            }

            // 配置 ssl
            if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
                client.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
            }

            if (logging) {
                netInterceptors.add(new HttpLoggerInterceptor());
            }

            // 配置全局拦截器
            if (!netInterceptors.isEmpty()) {
                netInterceptors.forEach(client::addNetworkInterceptor);
            }
            if (!interceptors.isEmpty()) {
                interceptors.forEach(client::addInterceptor);
            }

            // 配置超时
            if (null != timeout) {
                client.connectTimeout(timeout).callTimeout(timeout).readTimeout(timeout).writeTimeout(timeout);
            }

            return new Pigeon(this);
        }
    }
}
