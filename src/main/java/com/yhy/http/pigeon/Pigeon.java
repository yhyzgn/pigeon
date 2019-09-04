package com.yhy.http.pigeon;

import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.http.HttpMethod;
import com.yhy.http.pigeon.offer.GsonConverter;
import com.yhy.http.pigeon.offer.GuavaCallAdapter;
import com.yhy.http.pigeon.offer.HttpLoggerInterceptor;
import okhttp3.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 12:34
 * version: 1.0.0
 * desc   :
 */
@SuppressWarnings("unchecked")
public class Pigeon {
    private final Map<Method, HttpMethod<?>> httpMethodMap = new ConcurrentHashMap<>();

    private HttpUrl host;
    private List<Interceptor> netInterceptors;
    private List<Interceptor> interceptors;
    private Map<String, Object> headers;
    private List<CallAdapter.Factory> callFactories;
    private List<Converter.Factory> converterFactories;
    private OkHttpClient.Builder client;

    private Pigeon(Builder builder) {
        this.host = builder.host;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.callFactories = builder.adapterFactories;
        this.converterFactories = builder.converterFactories;
        this.client = builder.client;
    }

    public HttpUrl host() {
        return host;
    }

    public List<Interceptor> netInterceptors() {
        return netInterceptors;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public Map<String, Object> headers() {
        return headers;
    }

    public CallAdapter<?, ?> adapter(Type returnType, Annotation[] annotations) {
        return findCallAdapter(returnType, annotations);
    }

    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        return findStringConverter(type, annotations);
    }

    public <T> Converter<T, RequestBody> requestConverter(Type type, Annotation[] methodAnnotations, Annotation[] parameterAnnotations) {
        return findRequestConverter(type, methodAnnotations, parameterAnnotations);
    }

    public <T> Converter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        return findResponseConverter(responseType, annotations);
    }

    public OkHttpClient.Builder client() {
        // 返回干净的builder，‘client’中只包含全局拦截器，而不含自定义拦截器的builder
        return new OkHttpClient.Builder(client.build());
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

    private <T> Converter<T, RequestBody> findRequestConverter(Type type, Annotation[] annotations, Annotation[] parameterAnnotations) {
        Converter<T, RequestBody> converter;
        // 从后向前查找
        for (int i = converterFactories.size() - 1; i >= 0; i--) {
            converter = (Converter<T, RequestBody>) converterFactories.get(i).requestBodyConverter(type, annotations, parameterAnnotations, this);
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
        HttpMethod<?> result = httpMethodMap.get(method);
        if (null != result) return result;
        synchronized (httpMethodMap) {
            result = httpMethodMap.get(method);
            if (null == result) {
                result = HttpMethod.parseAnnotations(this, method);
                httpMethodMap.put(method, result);
            }
        }
        return result;
    }

    public static class Builder {
        private HttpUrl host;
        private List<Interceptor> netInterceptors = new ArrayList<>();
        private List<Interceptor> interceptors = new ArrayList<>();
        private Map<String, Object> headers = new HashMap<>();
        private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
        private List<Converter.Factory> converterFactories = new ArrayList<>();
        private OkHttpClient.Builder client;
        private boolean logging = true;

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

        public Builder header(String name, Object value) {
            this.headers.put(name, value);
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

        public Builder client(OkHttpClient.Builder client) {
            this.client = client;
            return this;
        }

        public Builder logging(boolean logging) {
            this.logging = logging;
            return this;
        }

        public Pigeon build() {
            if (null == host) {
                throw new IllegalStateException("host can not be null.");
            }

            adapterFactories.add(new GuavaCallAdapter());
            converterFactories.add(new GsonConverter());
            if (logging) {
                netInterceptors.add(new HttpLoggerInterceptor());
            }

            if (null == client) {
                client = new OkHttpClient.Builder();
            }

            // 配置全局拦截器
            if (!netInterceptors.isEmpty()) {
                netInterceptors.forEach(client::addNetworkInterceptor);
            }
            if (!interceptors.isEmpty()) {
                interceptors.forEach(client::addInterceptor);
            }

            return new Pigeon(this);
        }
    }
}
