package com.yhy.http.pigeon.internal;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.provider.HeaderProvider;

import java.lang.reflect.Constructor;

/**
 * 普通反射构造函数请求头提供者
 * <p>
 * Created on 2021-04-19 15:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ConstructorHeaderProvider implements HeaderProvider {

    @Override
    public Header.Dynamic provide(Class dynamicHeaderClass) throws Exception {
        Constructor<? extends Header.Dynamic> constructor = dynamicHeaderClass.getConstructor();
        return constructor.newInstance();
    }

    public static ConstructorHeaderProvider create() {
        return new ConstructorHeaderProvider();
    }
}
