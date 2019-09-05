package pigeon.interceptor;

import com.yhy.http.pigeon.offer.HttpLoggerInterceptor;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-05 10:05
 * version: 1.0.0
 * desc   :
 */
public class TestInterceptor implements Interceptor {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpLoggerInterceptor.class);

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        LOGGER.info("[TestInterceptor] executed !");
        return chain.proceed(chain.request());
    }
}
