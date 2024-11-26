package com.yhy.http.pigeon.spring.converter;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.internal.converter.JacksonConverter;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

/**
 * 配合 Spring 使用的转换器，主要解决 Spring 配置文件自动读取
 * <p>
 * Created on 2021-11-03 20:55
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class SpringConverter extends JacksonConverter {
    private final Environment environment;

    public SpringConverter(Environment environment) {
        this.environment = environment;
    }

    @Override
    @Nullable
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Pigeon pigeon) {
        return new StringPlaceholderConverter<>(environment);
    }

    private record StringPlaceholderConverter<T>(Environment environment) implements Converter<T, String> {

        @Nullable
        @Override
        public String convert(T from) {
            String text = from.toString();
            // 判断处理 Spring 配置变量 ${xxx.xxx}
            if (Pattern.matches("^\\$\\{\\s*[0-9a-zA-Z\\-_.]+\\s*}$", text)) {
                return environment.resolvePlaceholders(text);
            }
            return text;
        }
    }
}
