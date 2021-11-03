package com.yhy.http.pigeon.internal.converter;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.converter.Converter;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-06-05 1:15 下午
 * version: 1.0.0
 * desc   : 内置字符串响应转换器
 */
public class StringResponseConverter extends JacksonConverter {

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        return new StringResponseBodyConverter();
    }

    private static class StringResponseBodyConverter implements Converter<ResponseBody, String> {
        @Nullable
        @Override
        public String convert(ResponseBody from) throws IOException {
            return from.string();
        }
    }
}
