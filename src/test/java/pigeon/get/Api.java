package pigeon.get;

import com.yhy.http.pigeon.annotation.method.GET;
import com.yhy.http.pigeon.annotation.param.Query;

import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 12:38
 * version: 1.0.0
 * desc   :
 */
public interface Api {

    @GET("/api/test/test")
    String test();

    @GET("/api/get/noParam")
    String noParam();

    @GET("/api/get/normal")
    Map<String, Object> normal(String name, int age);

    @GET("/api/get/annotation")
    Map<String, Object> annotation(@Query String name, int age);
}
