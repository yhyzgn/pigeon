package com.yhy.http.pigeon.spring.starter.simple.api.controller;

import com.tengyun.saas.lib.util.response.Res;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Res get(@RequestHeader(value = "Token", required = false) String token) {
        log.info("token = {}", token);
        return Res.success(token, "成功");
    }
}
