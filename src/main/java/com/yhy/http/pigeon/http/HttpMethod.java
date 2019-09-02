package com.yhy.http.pigeon.http;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.http.request.RequestFactory;
import com.yhy.http.pigeon.utils.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 15:26
 * version: 1.0.0
 * desc   :
 */
public abstract class HttpMethod<T> {

    public static HttpMethod<?> parseAnnotations(Pigeon pigeon, Method method) {
        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw Utils.methodError(method, "Method return type must not include a type variable or wildcard: %s", returnType);
        }
        if (returnType == void.class) {
            throw Utils.methodError(method, "Service methods cannot return void.");
        }
        return HttpHandler.parseAnnotations(pigeon, method, RequestFactory.parseAnnotations(pigeon, method));
    }

    public abstract T invoke(Object[] objects);
}
