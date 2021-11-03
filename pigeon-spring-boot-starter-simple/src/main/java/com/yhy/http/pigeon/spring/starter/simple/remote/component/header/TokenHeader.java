package com.yhy.http.pigeon.spring.starter.simple.remote.component.header;

import com.tengyun.saas.lib.util.core.RandUtils;
import com.yhy.http.pigeon.annotation.Header;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2021-11-03 15:12
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class TokenHeader implements Header.Dynamic {
    @Autowired
    private ApplicationContext context;

    @Override
    public Map<String, String> pairs(Method method) {
        log.info("autowired applicationContext = {}", context);

        Map<String, String> result = new HashMap<>();
        result.put("Token", RandUtils.get(32));
        return result;
    }
}
