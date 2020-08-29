package com.yhy.http.pigeon.internal;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-29 19:40
 * version: 1.0.0
 * desc   : 公共请求头专用拦截器
 */
public class HeaderInterceptor implements Interceptor {
    private final static Logger LOGGER = LoggerFactory.getLogger(HeaderInterceptor.class);

    private final Map<String, String> headers;

    public HeaderInterceptor(Map<String, String> headers) {
        this.headers = headers;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request original = chain.request();
        // 重建 request 并注入 全局请求头
        Headers.Builder builder = original.headers().newBuilder();
        headers.forEach((name, value) -> {
            LOGGER.debug("Setting global request header: {} = {}", name, value);
            builder.set(name, value);
        });
        Request request = original.newBuilder().headers(builder.build()).build();
        // 继续请求
        return chain.proceed(request);
    }
}
