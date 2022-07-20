package com.yhy.http.pigeon.spring.starter.simple.utils;

import java.lang.reflect.Array;
import java.util.function.Predicate;

/**
 * 数组工具类
 * <p>
 * Created on 2020-08-24 14:49
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ArrayUtils {

    /**
     * 快速创建数组
     *
     * @param ts  元素
     * @param <T> 数组类型
     * @return 创建结果
     */
    @SafeVarargs
    public static <T> T[] of(T... ts) {
        return ts;
    }

    /**
     * 查询数组元素索引
     *
     * @param src 数组
     * @param obj 要查找的元素
     * @param <T> 数组元素类型
     * @return 索引
     */
    public static <T> int indexOf(T[] src, T obj) {
        return indexOf(src, t -> t.equals(obj));
    }

    /**
     * 查询数组元素索引
     *
     * @param src       数组
     * @param predicate 条件构造器
     * @param <T>       数组元素类型
     * @return 索引
     */
    public static <T> int indexOf(T[] src, Predicate<T> predicate) {
        for (int i = 0; i < src.length; i++) {
            if (predicate.test(src[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 数组切割成二维数组
     *
     * @param src      原数组
     * @param capacity 每个子数组容量
     * @param <T>      类型
     * @return 结果
     */
    public static <T> T[][] cut(T[] src, int capacity) {
        return cut(src, capacity, false);
    }

    /**
     * 数组切割成二维数组
     *
     * @param src      原数组
     * @param capacity 每个子数组容量
     * @param zoom     当子数组容量大于原数组长度时，是否允许自动调整
     * @param <T>      类型
     * @return 结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] cut(T[] src, int capacity, boolean zoom) {
        if (null == src) {
            return null;
        }
        if (capacity < 1) {
            capacity = 1;
        }
        int length = src.length;
        // 自动缩放
        if (zoom && capacity > length) {
            capacity = length;
        }
        int count = length % capacity == 0 ? length / capacity : length / capacity + 1;
        Class<?> clazz = src.getClass().getComponentType();

        T[][] result = (T[][]) Array.newInstance(clazz, count, 0);
        for (int i = 0, start = 0, end = Math.min(capacity, length); i < count; i++, start = i
                * capacity, end = i < count - 1 ? start + capacity : length) {
            result[i] = (T[]) Array.newInstance(clazz, zoom ? end - start : capacity);
            System.arraycopy(src, start, result[i], 0, end - start);
        }
        return result;
    }
}
