package com.yhy.http.pigeon.converter;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.utils.Utils;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 16:56
 * version: 1.0.0
 * desc   :
 */
public interface Converter<F, T> {

    @Nullable
    T convert(F from) throws IOException;

    abstract class Factory {

        @Nullable
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] methodAnnotations, Annotation[] parameterAnnotations, Pigeon pigeon) {
            return null;
        }

        @Nullable
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
            return null;
        }

        @Nullable
        public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
            return null;
        }

        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }
}
