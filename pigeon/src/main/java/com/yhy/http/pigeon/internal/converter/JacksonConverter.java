package com.yhy.http.pigeon.internal.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.converter.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-06-19 10:15
 * version: 1.0.0
 * desc   : 内置转换器-Jackson
 */
public class JacksonConverter extends Converter.Factory {

    private final ObjectMapper mapper;

    public JacksonConverter() {
        this(new ObjectMapper());
    }

    public JacksonConverter(ObjectMapper mapper) {
        // 排除json字符串中实体类没有的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
    }

    @Override
    public @Nullable Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] methodAnnotations, Annotation[] parameterAnnotations, Pigeon pigeon) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonRequestBodyConverter<>(mapper, javaType);
    }

    @Override
    public @Nullable Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonResponseBodyConverter<>(mapper, javaType);
    }

    @Override
    public @Nullable Converter<?, String> stringConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        return new StringConverter<>();
    }

    private static final class JacksonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        private final ObjectMapper mapper;
        private final JavaType type;

        private JacksonRequestBodyConverter(ObjectMapper mapper, JavaType type) {
            this.mapper = mapper;
            this.type = type;
        }

        @Override
        public @NotNull RequestBody convert(T from) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonGenerator gen = mapper.writer().forType(type).createGenerator(writer);
            mapper.writeValue(gen, from);
            gen.close();
            return RequestBody.create(MEDIA_TYPE, buffer.readByteArray());
        }
    }

    private static final class JacksonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final ObjectMapper mapper;
        private final JavaType type;

        private JacksonResponseBodyConverter(ObjectMapper mapper, JavaType type) {
            this.mapper = mapper;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            // 如果目标类型是String，则直接返回，避免jackson出现不识别无双引号的字符串类型
            if (type.getRawClass() == String.class) {
                return (T) from.string();
            }
            return mapper.readValue(from.byteStream(), type);
        }
    }

    private static final class StringConverter<T> implements Converter<T, String> {

        @Nullable
        @Override
        public String convert(T from) throws IOException {
            return from.toString();
        }
    }
}
