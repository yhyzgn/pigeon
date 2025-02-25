package com.yhy.http.pigeon.spring.delegate;

import com.yhy.http.pigeon.delegate.MethodAnnotationDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass) {
        MergedAnnotations annotations = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
        // 使用 stream() 获取所有 Header 注解
        List<T> result = annotations.stream(annotationClass)
                .map(MergedAnnotation::synthesize) // 合成每个注解实例
                .toList();
        return (CollectionUtils.isEmpty(result) ? Collections.singletonList(method.getAnnotation(annotationClass)) : result).stream().filter(Objects::nonNull).toList();
    }
}
