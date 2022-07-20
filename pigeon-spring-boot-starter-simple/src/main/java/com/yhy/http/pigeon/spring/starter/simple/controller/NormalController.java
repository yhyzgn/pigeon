package com.yhy.http.pigeon.spring.starter.simple.controller;

import com.tengyun.saas.lib.util.response.Res;
import com.yhy.http.pigeon.spring.starter.simple.remote.NormalAPI;
import com.yhy.http.pigeon.spring.starter.simple.utils.CodeUtils;
import com.yhy.http.pigeon.spring.starter.simple.utils.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2021-05-22 18:03
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/normal")
public class NormalController {

    @Autowired
    private NormalAPI normalAPI;

    @Autowired
    private SnowFlake snowFlake;

    @GetMapping("/get")
    public Res get() {
        System.out.println(snowFlake.next());
        System.out.println(CodeUtils.next(6));
        return normalAPI.get();
    }
}
