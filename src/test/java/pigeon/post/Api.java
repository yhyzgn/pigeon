package pigeon.post;

import com.yhy.http.pigeon.annotation.Form;
import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Interceptor;
import com.yhy.http.pigeon.annotation.method.POST;
import com.yhy.http.pigeon.annotation.param.Body;
import com.yhy.http.pigeon.annotation.param.Field;
import com.yhy.http.pigeon.annotation.param.Path;
import com.yhy.http.pigeon.annotation.param.Query;
import pigeon.Rmt;
import pigeon.get.Cat;
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

    @POST("/api/post/noParam")
    String noParam();

    @POST("/api/post/normal")
    @Interceptor(TestInterceptor.class)
    Map<String, Object> normal(String name, int age);

    @POST("/api/post/annotation")
    @Form
    Map<String, Object> annotation(@Query String name, int age, @Field("remark") String ext);

    @POST("/api/post/path/{id}/{count}")
    Map<String, Object> path(@Path("id") String alias, @Path int count, String remark);

    @POST("/api/post/cat")
    Cat cat(@Body Cat cat);

    @POST("/api/post/cat")
    @Form
    Cat mp(@Header Map<String, Object> header, @Field Map<String, Object> params);

    @POST
    @Form
    Rmt<String> form(@Header Map<String, Object> header, @Field String field);

    @POST
    Rmt<Cat> body(@Header Map<String, Object> header, @Body Map<String, Object> body);
}
