package com.yhy.http.pigeon.offer;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 18:14
 * version: 1.0.0
 * desc   :
 */
public class HttpLoggerInterceptor implements Interceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpLoggerInterceptor.class);
    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E", Locale.getDefault());

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        String method = request.method();
        Headers headers = request.headers();
        Set<String> names = headers.names();
        Iterator<String> it = names.iterator();

        LogLines lines = LogLines.start("Request starting in {}", FORMAT.format(new Date())).line("url : {}", url).line("method : {}", method);

        lines.line("").line("-- Request Header --");
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line("{} : {}", name, value);
        }

        Response response = chain.proceed(request);
        headers = response.headers();
        names = headers.names();
        it = names.iterator();
        lines.empty().line("-- Response Header --");
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line("{} : {}", name, value);
        }

        ResponseBody body = response.body();
        if (null != body) {
            BufferedSource source = body.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();
            String result = buffer.clone().readString(StandardCharsets.UTF_8);
            lines.empty().line("-- Response Body --");
            lines.line(result);
        }

        lines.empty().line("-- Http Finished --");

        log(null != request.tag() ? request.tag() : this, lines);
        return response;
    }

    private HttpLoggerInterceptor log(Object tag, LogLines lines) {
        LOGGER.info("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        LOGGER.info("│ " + tag.toString());
        LOGGER.info("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄");
        lines.lines().forEach(item -> {
            LOGGER.info("│ " + item.msg, item.args);
        });
        LOGGER.info("└────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        return this;
    }

    private static class LogLines {
        private static List<LogLine> lines = new ArrayList<>();

        static LogLines start(String msg, Object... args) {
            lines.add(new LogLine(msg, args));
            return new LogLines();
        }

        LogLines line(String msg, Object... args) {
            lines.add(new LogLine(msg, args));
            return this;
        }

        LogLines empty() {
            lines.add(new LogLine(""));
            return this;
        }

        List<LogLine> lines() {
            List<LogLine> temp = new ArrayList<>(lines);
            lines.clear();
            return temp;
        }
    }

    private static class LogLine {
        String msg;
        Object[] args;

        private LogLine(String msg, Object... args) {
            this.msg = msg;
            this.args = args;
        }
    }
}
