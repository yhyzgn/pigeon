package com.yhy.http.pigeon.delegate;

import com.yhy.http.pigeon.annotation.Header;

/**
 * 请求头提供者
 * <p>
 * Created on 2021-04-19 14:54
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HeaderDelegate {

    /**
     * 从下层逻辑获取动态请求头
     *
     * @param dynamicHeaderClass 动态请求头类
     * @param <T>                请求头类
     * @return 动态请求头对象
     * @throws Exception 可能出现的异常
     */
    <T extends Header.Dynamic> T apply(Class<T> dynamicHeaderClass) throws Exception;
}
