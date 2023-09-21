package com.yhy.http.pigeon.spring.starter.simple.api.controller;

import com.yhy.jakit.util.internal.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created on 2021-05-22 18:03
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/normal")
public class NormalController {

    @GetMapping("/get")
    public Map<String, Object> get(@RequestHeader(value = "Token", required = false) String token, String[] codes) {
        log.info("token = {}, codes = {}", token, codes);
        return Maps.of("code", 200, "message", "成功", "data", codes);
    }
}
