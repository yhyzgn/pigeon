package pigeon.get;

import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Interceptor;
import com.yhy.http.pigeon.annotation.method.GET;
import com.yhy.http.pigeon.annotation.param.Path;
import com.yhy.http.pigeon.annotation.param.Query;
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

    @GET("/api/test/test")
    String test();

    @GET("/api/get/noParam")
    String noParam();

    @GET("/api/get/normal")
    @Interceptor(TestInterceptor.class)
    Map<String, Object> normal(String name, int age);

    @GET("/api/get/annotation")
    Map<String, Object> annotation(@Query String name, int age, @Query("remark") String ext);

    @GET("/api/get/path/{id}/{count}")
    Map<String, Object> path(@Path("id") String alias, @Path int count, String remark);

    @GET("/api/get/cat")
    Cat cat(@Query String name, int age, @Query("remark") String ext);

    @GET("/api/get/cat")
    Cat mp(@Header Map<String, Object> header, @Query Map<String, Object> params);

    @GET
    @Interceptor(value = TestInterceptor.class, net = true)
    Cat def(@Header Map<String, Object> header, @Query Map<String, Object> params);
}
