package pigeon.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-05 10:05
 * version: 1.0.0
 * desc   :
 */
public class TestInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        System.out.println("[TestInterceptor] executed !");
        Request request = chain.request();
        System.out.println(request.header("XX"));
        return chain.proceed(request.newBuilder().build());
    }
}
