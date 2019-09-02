package com.yhy.http.pigeon.http.request.param;

import com.yhy.http.pigeon.http.request.RequestBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 16:44
 * version: 1.0.0
 * desc   :
 */
public abstract class ParameterHandler<T> {

    abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;
}
