package pigeon.get;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.internal.StringResponseConverter;
import pigeon.Rmt;
import pigeon.interceptor.TestInterceptor;

import java.io.IOException;
import java.util.HashMap;
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
        Pigeon pigeon = new Pigeon.Builder().host("https://www.baidu.com").addConverterFactory(new StringResponseConverter()).header("XX", "XX-Header-Value").build();
//        Pigeon pigeon = new Pigeon.Builder().host("http://localhost:8080").netInterceptor(new TestInterceptor()).header("XX", "asdfasdf").build();
//        Pigeon pigeon = new Pigeon.Builder().host("https://t-tio.ybsjyyn.com/reporter/api/debug").netInterceptor(new TestInterceptor()).header("XX", "asdfasdf").build();
        Api api = pigeon.create(Api.class);

//        String test = api.test();
//        System.out.println(test);
//
//        String noParam = api.noParam();
//        System.out.println(noParam);
//
//        Map<String, Object> normal = api.normal("张三", 5);
//        System.out.println(normal);
//
//        Map<String, Object> annotation = api.annotation("张三1", 6, "附加信息");
//        System.out.println(annotation);
//
//        Map<String, Object> path = api.path("abcd", 6, "附加信息");
//        System.out.println(path);
//
//        Cat cat = api.cat("张三-cat", 6, "附加信息");
//        System.out.println(cat);

        Map<String, Object> header = new HashMap<>();
        header.put("UC", "Pigeon");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "李四");
        params.put("age", "8");
//        params.put("remark", "附加信息");

//        Cat cat = api.mp(header, params);
//        System.out.println(cat);

//        String cat = api.def(header, params);
//        System.out.println(cat);
//        cat = api.def(header, params);
//        System.out.println(cat);
//        cat = api.def(header, params);
//        System.out.println(cat);

//        api.cat("asdf", 23, null);

        String cat = api.rmt(header, params);
//        System.out.println(cat);

        System.out.println("=========================================");

        cat = api.rmt(header, params);
//        System.out.println(cat);
    }
}
