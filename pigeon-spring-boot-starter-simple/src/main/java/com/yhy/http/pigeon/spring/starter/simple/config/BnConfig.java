package com.yhy.http.pigeon.spring.starter.simple.config;

import com.yhy.http.pigeon.spring.starter.simple.utils.SnowFlake;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2022-07-19 9:31
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class BnConfig {

    @Bean
    @ConditionalOnMissingBean
    public SnowFlake snowFlake() {
        return SnowFlake.create(8, 12);
    }
}
