package com.yhy.http.pigeon.http;

import com.yhy.http.pigeon.common.Response;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 11:20
 * version: 1.0.0
 * desc   :
 */
public class HttpException extends RuntimeException {
    private final int code;
    private final String message;
    private final transient Response<?> response;

    public HttpException(Response<?> response) {
        super(getMessage(response));
        this.code = response.code();
        this.message = response.message();
        this.response = response;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Nullable
    public Response<?> response() {
        return response;
    }

    private static String getMessage(Response<?> response) {
        Objects.requireNonNull(response, "response == null");
        return "HTTP " + response.code() + " " + response.message();
    }
}
