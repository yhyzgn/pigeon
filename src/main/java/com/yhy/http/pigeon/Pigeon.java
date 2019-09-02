package com.yhy.http.pigeon;

import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.http.HttpMethod;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.ResponseBody;

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

    private HttpUrl baseUrl;
    private List<Interceptor> interceptors;
    private Map<String, Object> headers;
    private CallAdapter.Factory callAdapterFactory;
    private Converter.Factory responseConverterFactory;

    private Pigeon(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
    }

    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public CallAdapter<?, ?> getCallAdapter(Type returnType, Annotation[] annotations) {
        return callAdapterFactory.get(returnType, annotations, this);
    }

    public <T> Converter<ResponseBody, T> getResponseConverter(Type responseType, Annotation[] annotations) {
        Converter<ResponseBody, ?> converter = responseConverterFactory.responseBodyConverter(responseType, annotations, this);
        return null != converter ? (Converter<ResponseBody, T>) converter : null;
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
        private HttpUrl baseUrl;
        private List<Interceptor> interceptors = new ArrayList<>();
        private Map<String, Object> headers = new HashMap<>();

        public Builder baseUrl(String url) {
            Objects.requireNonNull(url, "URL can not be null.");
            this.baseUrl = HttpUrl.get(url);
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder header(String name, Object value) {
            this.headers.put(name, value);
            return this;
        }

        public Pigeon build() {
            return new Pigeon(this);
        }
    }
}
