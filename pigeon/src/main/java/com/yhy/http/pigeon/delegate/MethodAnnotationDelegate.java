package com.yhy.http.pigeon.delegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * method 注解提取接口
 * <p>
 * Created on 2025-02-24 16:29
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MethodAnnotationDelegate {

    /**
     * 获取方法注解
     *
     * @param method          方法
     * @param annotationClass 注解类
     * @return 注解实例
     */
    <T extends Annotation> Optional<T> apply(Method method, Class<T> annotationClass);
}
