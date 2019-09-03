package com.yhy.http.pigeon;

import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.def.DefCallAdapter;
import com.yhy.http.pigeon.def.DefCallFactory;
import com.yhy.http.pigeon.def.DefConverter;
import com.yhy.http.pigeon.http.HttpMethod;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
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

    private HttpUrl host;
    private List<Interceptor> interceptors;
    private Map<String, Object> headers;
    private CallAdapter.Factory callAdapterFactory;
    private Converter.Factory stringConverterFactory;
    private Converter.Factory requestConverterFactory;
    private Converter.Factory responseConverterFactory;
    private okhttp3.Call.Factory callFactory;

    private Pigeon(Builder builder) {
        this.host = builder.host;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.callAdapterFactory = builder.callAdapterFactory;
        this.stringConverterFactory = builder.stringConverterFactory;
        this.requestConverterFactory = builder.requestConverterFactory;
        this.responseConverterFactory = builder.responseConverterFactory;
        this.callFactory = builder.callFactory;
    }

    public HttpUrl host() {
        return host;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public Map<String, Object> headers() {
        return headers;
    }

    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return callAdapterFactory.get(returnType, annotations, this);
    }

    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        Converter<?, String> converter = stringConverterFactory.stringConverter(type, annotations, this);
        return null != converter ? (Converter<T, String>) converter : null;
    }

    public <T> Converter<T, RequestBody> requestConverter(Type type, Annotation[] methodAnnotations, Annotation[] parameterAnnotations) {
        Converter<?, RequestBody> converter = requestConverterFactory.requestBodyConverter(type, methodAnnotations, parameterAnnotations, this);
        return null != converter ? (Converter<T, RequestBody>) converter : null;
    }

    public <T> Converter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        Converter<ResponseBody, ?> converter = responseConverterFactory.responseBodyConverter(responseType, annotations, this);
        return null != converter ? (Converter<ResponseBody, T>) converter : null;
    }

    public okhttp3.Call.Factory callFactory() {
        return callFactory;
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
        private HttpUrl host;
        private List<Interceptor> interceptors = new ArrayList<>();
        private Map<String, Object> headers = new HashMap<>();
        private CallAdapter.Factory callAdapterFactory = new DefCallAdapter();
        private Converter.Factory stringConverterFactory = new DefConverter();
        private Converter.Factory requestConverterFactory = new DefConverter();
        private Converter.Factory responseConverterFactory = new DefConverter();
        private okhttp3.Call.Factory callFactory = new DefCallFactory();

        public Builder host(String url) {
            Objects.requireNonNull(url, "URL can not be null.");
            this.host = HttpUrl.get(url);
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

        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.callAdapterFactory = factory;
            return this;
        }

        public Builder stringConverterFactory(Converter.Factory factory) {
            this.stringConverterFactory = factory;
            return this;
        }

        public Builder requestConverterFactory(Converter.Factory factory) {
            this.requestConverterFactory = factory;
            return this;
        }

        public Builder responseConverterFactory(Converter.Factory factory) {
            this.responseConverterFactory = factory;
            return this;
        }

        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = factory;
            return this;
        }

        public Pigeon build() {
            return new Pigeon(this);
        }
    }
}
