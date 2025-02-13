package com.yhy.http.pigeon.spring.starter.simple.api.controller;

import com.yhy.jakit.util.internal.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/get/{id}")
    public Map<String, Object> get(@RequestHeader(value = "Token", required = false) String token, String[] codes, @RequestHeader("Get-Id") Integer getId, @PathVariable Integer id) {
        log.info("token = {}, codes = {}, getId = {}, id = {}", token, codes, getId, id);
        return Maps.of("code", 200, "message", "成功", "data", codes);
    }
}
