package com.yhy.http.pigeon.spring.starter.simple.remote.component.interceptor;

import com.yhy.jakit.util.RandUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p>
 * Created on 2024-11-27 10:00
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class TokenInterceptor implements Interceptor {

    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(
                request.newBuilder()
                        .header("Authorization", "Bearer " + RandUtils.getString(32))
                        .build()
        );
    }
}
