import com.yhy.http.pigeon.annotation.Header;
import com.yhy.http.pigeon.annotation.Headers;
import com.yhy.http.pigeon.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 12:23
 * version: 1.0.0
 * desc   :
 */
public class AnnotationTester {

    @Headers({
            @Header("aa"),
            @Header("bb"),
            @Header("cc"),
            @Header("dd")
    })
    public void test(Map<String, Object> test) {
    }

    public static void main(String[] args) throws Exception {
//        Class clazz = AnnotationTester.class;
//        Method test = clazz.getDeclaredMethod("test", Map.class);
//        Header[] headers = test.getAnnotationsByType(Header.class);
//        System.out.println(headers.length);
//
//        Annotation[] annotations = test.getAnnotations();
//
//        Parameter[] parameters = test.getParameters();
//        for (Parameter parameter : parameters) {
//            Type type = parameter.getParameterizedType();
//            System.out.println(type.getTypeName());
//            Class<?> rawParameterType = Utils.getRawType(type);
//            System.out.println(Map.class.isAssignableFrom(rawParameterType));
//            System.out.println(rawParameterType.getTypeName());
//        }
//
//        System.out.println(annotations.length);
//        for (Annotation annotation : annotations) {
//            System.out.println(annotation instanceof Header);
//        }

        System.out.println(dispatchEncode("哈哈哈"));
    }

    private static String dispatchEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
