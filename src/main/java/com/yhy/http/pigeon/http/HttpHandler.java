package com.yhy.http.pigeon.http;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.http.request.RequestFactory;
import okhttp3.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 17:31
 * version: 1.0.0
 * desc   :
 */
@SuppressWarnings("unchecked")
public class HttpHandler<Res, Ret> extends HttpMethod<Ret> {

    public static <Res, Ret> HttpHandler<Res, Ret> parseAnnotations(Pigeon pigeon, Method method, RequestFactory factory) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();

        CallAdapter<Res, Ret> callAdapter = createCallAdapter(pigeon, annotations, returnType);
        Type responseType = callAdapter.responseType();
        Converter<ResponseBody, Res> responseConverter = createResponseConverter(pigeon, annotations, responseType);

        return null;
    }

    private static <Res> Converter<ResponseBody, Res> createResponseConverter(Pigeon pigeon, Annotation[] annotations, Type responseType) {
        return (Converter<ResponseBody, Res>) pigeon.getResponseConverter(responseType, annotations);
    }

    private static <Res, Ret> CallAdapter<Res, Ret> createCallAdapter(Pigeon pigeon, Annotation[] annotations, Type returnType) {
        return (CallAdapter<Res, Ret>) pigeon.getCallAdapter(returnType, annotations);
    }

    @Override
    public Ret invoke(Object[] objects) {
        return null;
    }
}
