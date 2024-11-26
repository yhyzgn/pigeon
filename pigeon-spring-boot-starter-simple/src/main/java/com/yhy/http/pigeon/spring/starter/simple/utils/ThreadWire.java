package com.yhy.http.pigeon.spring.starter.simple.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.lang.ref.WeakReference;

public class ThreadWire<T> {
    // 弱引用的 ThreadLocal
    private WeakReference<ThreadLocal<T>> ref;

    /**
     * 无参数构造方法
     */
    public ThreadWire() {
        ref = new WeakReference<>(new TransmittableThreadLocal<>());
    }

    /**
     * 带初始值的构造方法
     *
     * @param t 初始值
     */
    public ThreadWire(T t) {
        ref = new WeakReference<>(TransmittableThreadLocal.withInitial(() -> t));
    }

    /**
     * 设置值
     *
     * @param t 当前线程下的值
     */
    public void set(T t) {
        ThreadLocal<T> local = ref.get();
        if (null == local) {
            local = new ThreadLocal<>();
        }
        local.set(t);
        ref = new WeakReference<>(local);
    }

    /**
     * 获取值
     *
     * @return 当前线程下的值
     */
    public T get() {
        ThreadLocal<T> local = ref.get();
        return null != local ? local.get() : null;
    }

    /**
     * 移除资源
     */
    public void remove() {
        ThreadLocal<T> local = ref.get();
        if (null != local) {
            local.remove();
        }
    }
}
