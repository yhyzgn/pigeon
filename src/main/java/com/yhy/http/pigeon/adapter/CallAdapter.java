package com.yhy.http.pigeon.adapter;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.common.Call;
import com.yhy.http.pigeon.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 17:41
 * version: 1.0.0
 * desc   :
 */
public interface CallAdapter<R, T> {

    Type responseType();

    T adapt(Call<R> call);

    abstract class Factory {

        @Nullable
        public abstract CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Pigeon pigeon);

        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }
}
