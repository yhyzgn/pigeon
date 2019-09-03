package com.yhy.http.pigeon.def;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.common.Call;
import com.yhy.http.pigeon.common.Callback;
import com.yhy.http.pigeon.http.HttpException;
import com.yhy.http.pigeon.common.Response;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 11:08
 * version: 1.0.0
 * desc   :
 */
public class DefCallAdapter extends CallAdapter.Factory {

    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Pigeon pigeon) {
        if (getRawType(returnType) != ListenableFuture.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException("ListenableFuture return type must be parameterized as ListenableFuture<Foo> or ListenableFuture<? extends Foo>");
        }
        Type innerType = getParameterUpperBound(0, (ParameterizedType) returnType);
        if (getRawType(innerType) != Response.class) {
            return new BodyCallAdapter<>(innerType);
        }
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException("Response must be parameterized as Response<Foo> or Response<? extends Foo>");
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) innerType);
        return new ResponseCallAdapter<>(responseType);
    }

    private static final class BodyCallAdapter<R> implements CallAdapter<R, ListenableFuture<R>> {
        private final Type responseType;

        BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public ListenableFuture<R> adapt(Call<R> call) {
            return new AbstractFuture<R>() {
                {
                    call.enqueue(new Callback<R>() {
                        @Override
                        public void onResponse(Call<R> call, Response<R> response) {
                            if (response.isSuccessful()) {
                                set(response.body());
                            } else {
                                setException(new HttpException(response));
                            }
                        }

                        @Override
                        public void onFailure(Call<R> call, Throwable t) {
                            setException(t);
                        }
                    });
                }

                @Override
                protected void interruptTask() {
                    super.interruptTask();
                    call.cancel();
                }
            };
        }
    }

    private static final class ResponseCallAdapter<R> implements CallAdapter<R, ListenableFuture<Response<R>>> {
        private final Type responseType;

        ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public ListenableFuture<Response<R>> adapt(Call<R> call) {
            return new AbstractFuture<Response<R>>() {
                {
                    call.enqueue(new Callback<R>() {
                        @Override
                        public void onResponse(Call<R> call, Response<R> response) {
                            set(response);
                        }

                        @Override
                        public void onFailure(Call<R> call, Throwable t) {
                            setException(t);
                        }
                    });
                }

                @Override
                protected void interruptTask() {
                    super.interruptTask();
                    call.cancel();
                }
            };
        }
    }
}
