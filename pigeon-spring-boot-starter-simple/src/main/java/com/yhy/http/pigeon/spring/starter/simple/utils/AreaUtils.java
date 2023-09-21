package com.yhy.http.pigeon.spring.starter.simple.utils;

import org.springframework.util.Assert;

/**
 * 行政区划工具类
 * <p>
 * Created on 2021-12-23 10:00
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AreaUtils {

    /**
     * 计算行政区划级别
     *
     * @param areaCode 行政区划编码
     * @return 级别
     */
    static int calcLevel(String areaCode) {
        Assert.notNull(areaCode, "'areaCode' must not be null.");
        switch (areaCode.length()) {
            case 6:
                return areaCode.endsWith("0000") ? 1 : areaCode.endsWith("00") ? 2 : 3;
            case 12:
                return areaCode.endsWith("0000000000") ? 1 : areaCode.endsWith("00000000") ? 2 : areaCode.endsWith("000000") ? 3 : areaCode.endsWith("0000") ? 4 : areaCode.endsWith("00") ? 5 : 6;
            default:
                throw new IllegalArgumentException("Illegal length of 'areaCode'");
        }
    }
}
