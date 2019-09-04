package pigeon.get;

import com.yhy.http.pigeon.Pigeon;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 12:39
 * version: 1.0.0
 * desc   :
 */
public class ApiTester {

    public static void main(String[] args) throws IOException {
        Pigeon pigeon = new Pigeon.Builder().host("http://localhost:8080").build();
        Api api = pigeon.create(Api.class);

//        String test = api.test();
//        System.out.println(test);

//        String noParam = api.noParam();
//        System.out.println(noParam);

//        Map<String, Object> normal = api.annotation("张三", 5);
//        System.out.println(normal);

        Map<String, Object> annotation = api.annotation("张三1", 6);
        System.out.println(annotation);
    }
}
