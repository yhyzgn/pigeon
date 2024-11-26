package pigeon.get;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Interceptor;
import com.yhy.http.pigeon.annotation.method.Get;
import com.yhy.http.pigeon.annotation.param.Path;
import com.yhy.http.pigeon.annotation.param.Query;
import pigeon.header.TimestampHeader;
import pigeon.interceptor.TestInterceptor;

import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 12:38
 * version: 1.0.0
 * desc   :
 */
public interface Api {

    @Get("/api/test/test")
    String test();

    @Get("/api/get/noParam")
    String noParam();

    @Get("/api/get/normal")
    @Interceptor(TestInterceptor.class)
    Map<String, Object> normal(String name, int age);

    @Get("/api/get/annotation")
    Map<String, Object> annotation(@Query String name, int age, @Query("remark") String ext);

    @Get("/api/get/path/{id}/{count}")
    Map<String, Object> path(@Path("id") String alias, @Path int count, String remark);

    @Get("/api/get/cat")
    @Header(dynamic = TimestampHeader.class)
    Cat cat(@Query String name, int age, @Query(value = "remark", defaultValue = "啊哈哈") String ext);

    @Get("/api/get/cat")
    Cat mp(@Header Map<String, Object> header, @Query Map<String, Object> params);

    @Get
    @Interceptor(value = TestInterceptor.class, net = true)
    String def(@Header Map<String, ?> header, @Query Map<String, Object> params);

    @Get
    @Interceptor(value = TestInterceptor.class, net = true)
    String rmt(@Header Map<String, ?> header, @Query Map<String, Object> params);
}
