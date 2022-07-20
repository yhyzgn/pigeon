package com.yhy.http.pigeon.spring.starter.simple.utils;

import java.util.Random;

/**
 * 验证码工具类
 * <p>
 * Created on 2021-01-29 15:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class CodeUtils {
    private static final Random RAND = new Random();

    public static String next(int length) {
        // 默认4位
        int code =
                length == 5 ? RAND.nextInt(89999) + 10000 :
                        length == 6 ? RAND.nextInt(899999) + 100000 :
                                RAND.nextInt(8999) + 1000;
        return String.valueOf(code);
    }
}
