package com.yhy.http.pigeon.offer;

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
 * desc   :
 */
public class GuavaCallAdapter extends CallAdapter.Factory {

    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Pigeon pigeon) {
        if (getRawType(returnType) != Response.class) {
            return new BodyCallAdapter<>(returnType);
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        return new ResponseCallAdapter<>(responseType);
    }

    private static final class BodyCallAdapter<R> implements CallAdapter<R, R> {
        private final Type responseType;

        BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public R adapt(Call<R> call, Object[] args) throws Exception {
            ListenableFuture<R> future = new AbstractFuture<R>() {
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
            return future.get();
        }
    }

    private static final class ResponseCallAdapter<R> implements CallAdapter<R, Response<R>> {
        private final Type responseType;

        ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Response<R> adapt(Call<R> call, Object[] args) throws Exception {
            ListenableFuture<Response<R>> future = new AbstractFuture<Response<R>>() {
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
            return future.get();
        }
    }
}
