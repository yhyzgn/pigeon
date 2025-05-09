package com.yhy.http.pigeon.internal.converter;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.converter.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
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
 * time   : 2019-09-03 11:01
 * version: 1.0.0
 * desc   : 内置转换器-Gson
 */
public class GsonConverter extends Converter.Factory {
    private final Gson gson;

    public GsonConverter() {
        this(new Gson());
    }

    public GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Pigeon pigeon) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter<>(gson, adapter);
    }

    @Nullable
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        return new StringConverter<>();
    }

    private record GsonRequestBodyConverter<T>(Gson gson, TypeAdapter<T> adapter) implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        @Override
        public RequestBody convert(T from) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            adapter.write(jsonWriter, from);
            jsonWriter.close();
            return RequestBody.create(buffer.readByteArray(), MEDIA_TYPE);
        }
    }

    private record GsonResponseBodyConverter<T>(Gson gson, TypeAdapter<T> adapter) implements Converter<ResponseBody, T> {

        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            JsonReader jsonReader = gson.newJsonReader(from.charStream());
            jsonReader.setStrictness(Strictness.LENIENT);
            try (from) {
                T result = adapter.read(jsonReader);
                if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                    throw new JsonIOException("JSON document was not fully consumed.");
                }
                return result;
            }
        }
    }

    private static final class StringConverter<T> implements Converter<T, String> {

        @Nullable
        @Override
        public String convert(T from) {
            return from.toString();
        }
    }
}