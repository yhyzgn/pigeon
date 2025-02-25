package com.yhy.http.pigeon.internal.delegate;

import com.yhy.http.pigeon.delegate.MethodAnnotationDelegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 内置默认实现
 * <p>
 * Created on 2025-02-24 16:30
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorMethodAnnotationDelegate implements MethodAnnotationDelegate {

    @Override
    public <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass) {
        return Stream.of(method.getAnnotation(annotationClass)).filter(Objects::nonNull).toList();
    }

    public static ConstructorMethodAnnotationDelegate create() {
        return new ConstructorMethodAnnotationDelegate();
    }
}
