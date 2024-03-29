package com.yhy.http.pigeon.spring.starter.simple.remote;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.method.GET;
import com.yhy.http.pigeon.annotation.param.Query;
import com.yhy.http.pigeon.spring.starter.annotation.Pigeon;
import com.yhy.http.pigeon.spring.starter.simple.remote.component.header.TokenHeader;

import java.util.Map;

/**
 * Created on 2021-05-22 18:01
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Pigeon(baseURL = "http://localhost:8888/api/normal")
public interface NormalAPI {

    @GET("/get")
    @Header(dynamic = TokenHeader.class)
    @Header(pairName = "Remote-Property", pairValue = "${remote.header}")
    Map<String, Object> get(@Query("codes") String[] codes);
}
