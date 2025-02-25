package com.yhy.http.pigeon.spring.delegate;

import com.yhy.http.pigeon.delegate.MethodAnnotationDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 请求头 Spring 注入 bean
 * <p>
 * Created on 2021-11-03 10:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class SpringMethodAnnotationDelegate implements MethodAnnotationDelegate {

    @Override
    public <T extends Annotation> Optional<T> apply(Method method, Class<T> annotationClass) {
        MergedAnnotations annotations = MergedAnnotations.from(method);
        return Optional.ofNullable(annotations.isPresent(annotationClass) ? annotations.get(annotationClass).synthesize() : method.getAnnotation(annotationClass));
    }
}
