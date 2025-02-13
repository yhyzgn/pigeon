package com.yhy.http.pigeon.internal.logging;

import com.yhy.http.pigeon.common.Invocation;
import com.yhy.http.pigeon.common.SystemClock;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 18:14
 * version: 1.0.0
 * desc   : 默认的日志打印器
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

        lines.empty().line("-- Request Header --");
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line("{} : {}", name, value);
        }

        RequestBody reqBody = request.body();
        if (null != reqBody) {
            lines.empty().line("-- Request Body --");
            lines.line(requestBodyToString(reqBody).replace(System.lineSeparator(), System.lineSeparator() + "│ "));
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
        Response wrapResponse = response;
        if (null != resBody) {
            MediaType contentType = resBody.contentType();
            String encoding = Optional.ofNullable(response.header("Content-Encoding")).orElse("");
            BufferedSource source;
            if ("gzip".equals(encoding)) {
                source = Okio.buffer(new GzipSource(resBody.source()));
            } else {
                source = resBody.source();
            }
            String content = source.readUtf8();
            lines.empty().line("-- Response Body --");
            lines.line(content.replace(System.lineSeparator(), System.lineSeparator() + "│ "));

            // 重组 Response
            // 须移除 Content-Encoding，因为当前 body 已解压
            wrapResponse = response.newBuilder().removeHeader("Content-Encoding").body(ResponseBody.create(content, contentType)).build();
        }

        // 结束时间
        long end = SystemClock.now();
        lines.empty().line("-- Http Pigeon Finished. Used {} millis. --", end - start);

        // tag
        Invocation tag = request.tag(Invocation.class);
        log(null != tag ? tag : this, lines);

        return wrapResponse;
    }

    private void log(Object tag, LogLines lines) {
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        sb
                .append("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────")
                .append(System.lineSeparator())
                .append("│ ").append(tag.toString())
                .append(System.lineSeparator())
                .append("├────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────")
                .append(System.lineSeparator());
        lines.lines().forEach(item -> sb.append("│ ").append(item.msg.contains("{}") ? String.format(item.msg.replace("{}", "%s"), item.args) : item.msg).append(System.lineSeparator()));
        sb.append("└────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        LOGGER.info(sb.toString());
    }

    private String requestBodyToString(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    @SuppressWarnings("SameParameterValue")
    private static class LogLines {
        private static final List<LogLine> lines = new ArrayList<>();

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
            this.args = null == args || args.length == 0 ? null : args;
        }
    }
}
