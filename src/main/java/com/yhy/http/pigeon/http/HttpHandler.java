package com.yhy.http.pigeon.http;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.common.Call;
import com.yhy.http.pigeon.common.OkCall;
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
public abstract class HttpHandler<Res, Ret> extends HttpMethod<Ret> {

    private final RequestFactory requestFactory;
    private final okhttp3.Call.Factory callFactory;
    private final Converter<ResponseBody, Res> responseConverter;

    private HttpHandler(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, Converter<ResponseBody, Res> responseConverter) {
        this.requestFactory = requestFactory;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    @Override
    public Ret invoke(Object[] args) {
        OkCall<Res> call = new OkCall<>(requestFactory, callFactory, responseConverter, args);
        return adapt(call, args);
    }

    protected abstract Ret adapt(Call<Res> call, Object[] args);

    public static <Res, Ret> HttpHandler<Res, Ret> parseAnnotations(Pigeon pigeon, Method method, RequestFactory factory) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        CallAdapter<Res, Ret> callAdapter = createCallAdapter(pigeon, annotations, returnType);
        Type responseType = callAdapter.responseType();
        Converter<ResponseBody, Res> responseConverter = createResponseConverter(pigeon, annotations, responseType);

        return new AdaptedCall<>(factory, pigeon.callFactory(), responseConverter, callAdapter);
    }

    private static <Res> Converter<ResponseBody, Res> createResponseConverter(Pigeon pigeon, Annotation[] annotations, Type responseType) {
        return (Converter<ResponseBody, Res>) pigeon.responseConverter(responseType, annotations);
    }

    private static <Res, Ret> CallAdapter<Res, Ret> createCallAdapter(Pigeon pigeon, Annotation[] annotations, Type returnType) {
        return (CallAdapter<Res, Ret>) pigeon.callAdapter(returnType, annotations);
    }

    public static class AdaptedCall<Res, Ret> extends HttpHandler<Res, Ret> {

        private final CallAdapter<Res, Ret> callAdapter;

        AdaptedCall(RequestFactory requestFactory, okhttp3.Call.Factory callFactory, Converter<ResponseBody, Res> responseConverter, CallAdapter<Res, Ret> callAdapter) {
            super(requestFactory, callFactory, responseConverter);
            this.callAdapter = callAdapter;
        }

        @Override
        protected Ret adapt(Call<Res> call, Object[] args) {
            return callAdapter.adapt(call);
        }
    }
}
