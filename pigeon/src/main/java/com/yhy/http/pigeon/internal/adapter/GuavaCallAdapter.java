package com.yhy.http.pigeon.internal.adapter;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.adapter.CallAdapter;
import com.yhy.http.pigeon.common.Call;
import com.yhy.http.pigeon.common.Callback;
import com.yhy.http.pigeon.common.Response;
import com.yhy.http.pigeon.http.HttpException;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 11:08
 * version: 1.0.0
 * desc   : 内置网络请求执行器-Guava
 */
public class GuavaCallAdapter extends CallAdapter.Factory {

    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Pigeon pigeon) {
        if (getRawType(returnType) != Response.class) {
            return new BodyCallAdapter<>(returnType);
        }
        Type responseType = getFirstParameterUpperBound((ParameterizedType) returnType);
        return new ResponseCallAdapter<>(responseType);
    }

    private record BodyCallAdapter<R>(Type responseType) implements CallAdapter<R, R> {

        @Override
        public R adapt(Call<R> call, Object[] args) throws Exception {
            ListenableFuture<R> future = new AbstractFuture<>() {
                {
                    call.enqueue(new Callback<>() {
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
            return future.get();
        }
    }

    private record ResponseCallAdapter<R>(Type responseType) implements CallAdapter<R, Response<R>> {

        @Override
        public Response<R> adapt(Call<R> call, Object[] args) throws Exception {
            ListenableFuture<Response<R>> future = new AbstractFuture<>() {
                {
                    call.enqueue(new Callback<>() {
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
            return future.get();
        }
    }
}
