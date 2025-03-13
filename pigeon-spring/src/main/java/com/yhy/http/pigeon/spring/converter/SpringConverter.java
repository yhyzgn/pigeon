package com.yhy.http.pigeon.spring.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.utils.Utils;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 配合 Spring 使用的转换器，主要解决 Spring 配置文件自动读取
 * <p>
 * Created on 2021-11-03 20:55
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SpringConverter extends Converter.Factory {
    private final ObjectMapper mapper;
    private final Environment environment;

    public @Nullable Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Pigeon pigeon) {
        JavaType javaType = this.mapper.getTypeFactory().constructType(type);
        return new JacksonRequestBodyConverter<>(this.mapper, javaType);
    }

    public @Nullable Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        JavaType javaType = this.mapper.getTypeFactory().constructType(type);
        return new JacksonResponseBodyConverter<>(this.mapper, javaType);
    }

    public @Nullable Converter<?, String> stringConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        return new StringPlaceholderConverter<>(environment);
    }

    private record JacksonRequestBodyConverter<T>(ObjectMapper mapper, JavaType type) implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8;

        public @NotNull RequestBody convert(T from) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonGenerator gen = this.mapper.writer().forType(this.type).createGenerator(writer);
            this.mapper.writeValue(gen, from);
            gen.close();
            return RequestBody.create(buffer.readByteArray(), MEDIA_TYPE);
        }

        static {
            UTF_8 = StandardCharsets.UTF_8;
        }
    }

    private record JacksonResponseBodyConverter<T>(ObjectMapper mapper, JavaType type) implements Converter<ResponseBody, T> {
        @SuppressWarnings("unchecked")
        public @Nullable T convert(ResponseBody from) throws IOException {
            return (T) (this.type.getRawClass() == String.class ? from.string() : this.mapper.readValue(from.byteStream(), this.type));
        }
    }

    private record StringPlaceholderConverter<T>(Environment environment) implements Converter<T, String> {

        @Nullable
        @Override
        public String convert(T from) {
            String text = from.toString();
            // 判断处理 Spring 配置变量 ${xxx.xxx}
            if (Utils.isPlaceholdersPresent(text)) {
                return environment.resolvePlaceholders(text);
            }
            return text;
        }
    }
}
