package com.yhy.http.pigeon.offer;

import com.yhy.http.pigeon.common.Invocation;
import com.yhy.http.pigeon.common.SystemClock;
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
        // 开始时间
        long start = SystemClock.now();

        // 获取http信息
        Request request = chain.request();
        HttpUrl url = request.url();
        String method = request.method();
        Headers headers = request.headers();
        Set<String> names = headers.names();
        Iterator<String> it = names.iterator();

        LogLines lines = LogLines.start("-- Request starting at {} --", FORMAT.format(new Date())).line("url : {}", url).line("method : {}", method);

        lines.line("").line("-- Request Header --");
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line("{} : {}", name, value);
        }

        RequestBody reqBody = request.body();
        if (null != reqBody) {
            lines.empty().line("-- Request Body --");
            lines.line(requestBodyToString(reqBody));
        }

        Response response = chain.proceed(request);
        headers = response.headers();
        names = headers.names();
        it = names.iterator();
        lines.empty().line("-- Response Header --");
        lines.line("Status : {}", response.code());
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line("{} : {}", name, value);
        }

        ResponseBody resBody = response.body();
        if (null != resBody) {
            BufferedSource source = resBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();
            String result = buffer.clone().readString(StandardCharsets.UTF_8);
            lines.empty().line("-- Response Body --");
            lines.line(result);
        }

        // 结束时间
        long end = SystemClock.now();
        lines.empty().line("-- Http Finished. Used {} millis. --", end - start);

        // tag
        Invocation tag = request.tag(Invocation.class);
        log(null != tag ? tag : this, lines);
        return response;
    }

    private void log(Object tag, LogLines lines) {
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        sb
                .append("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────")
                .append(System.lineSeparator())
                .append("│ ").append(tag.toString())
                .append(System.lineSeparator())
                .append("├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄")
                .append(System.lineSeparator());
        lines.lines().forEach(item -> {
            sb.append("│ ").append(String.format(item.msg.replace("{}", "%s"), item.args)).append(System.lineSeparator());
        });
        sb.append("└────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        LOGGER.info(sb.toString());
    }

    private String requestBodyToString(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
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
