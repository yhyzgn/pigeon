package com.yhy.http.pigeon.http.request;

import com.google.gson.internal.LinkedTreeMap;
import com.yhy.http.pigeon.utils.Utils;
import okhttp3.*;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 16:46
 * version: 1.0.0
 * desc   :
 */
public class RequestBuilder {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String PATH_SEGMENT_ALWAYS_ENCODE_SET = " \"<>^`{}|\\?#";
    private static final Pattern PATH_TRAVERSAL = Pattern.compile("(.*/)?(\\.|%2e|%2E){1,2}(/.*)?");

    private final HttpUrl host;
    private final String method;
    private final boolean hasBody;
    private final Request.Builder requestBuilder;
    private final Headers.Builder headersBuilder;

    private String relativeUrl;

    private MediaType contentType;

    private MultipartBody.Builder multipartBuilder;
    private FormBody.Builder formBuilder;
    private RequestBody body;

    private final Map<String, String> pathParamMap;
    private final Map<String, String> queryParamMap;
    private final Map<String, String> fieldParamMap;

    RequestBuilder(String method, HttpUrl host, @Nullable String relativeUrl, @Nullable Headers headers, @Nullable MediaType contentType, boolean hasBody, boolean isForm, boolean isMultipart) {
        this.method = method;
        this.host = host;
        this.relativeUrl = relativeUrl;
        this.requestBuilder = new Request.Builder();
        this.contentType = contentType;
        this.hasBody = hasBody;
        if (headers != null) {
            headersBuilder = headers.newBuilder();
        } else {
            headersBuilder = new Headers.Builder();
        }
        // Form 和 Multipart 设置 body
        if (isForm) {
            formBuilder = new FormBody.Builder();
        } else if (isMultipart) {
            multipartBuilder = new MultipartBody.Builder();
            multipartBuilder.setType(MultipartBody.FORM);
        }
        // 临时记录各种参数
        pathParamMap = new LinkedTreeMap<>();
        queryParamMap = new LinkedTreeMap<>();
        fieldParamMap = new LinkedTreeMap<>();
    }

    public void setRelativeUrl(Object url) {
        this.relativeUrl = url.toString();
    }

    public void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            contentType = MediaType.get(value);
        } else {
            headersBuilder.add(name, value);
        }
    }

    public void addHeaders(Headers headers) {
        headersBuilder.addAll(headers);
    }

    public void addPathParam(String name, String value, boolean encoded) {
        pathParamMap.put(name, dispatchEncode(value, encoded));
    }

    public void addQueryParam(String name, String value, boolean encoded) {
        queryParamMap.put(name, dispatchEncode(value, encoded));
    }

    public void addFiled(String name, String value, boolean encoded) {
        fieldParamMap.put(name, dispatchEncode(value, encoded));
    }

    public void addPart(Headers headers, RequestBody body) {
        multipartBuilder.addPart(headers, body);
    }

    public void addPart(MultipartBody.Part part) {
        multipartBuilder.addPart(part);
    }

    public void setBody(RequestBody body) {
        this.body = body;
    }

    public <T> void addTag(Class<T> cls, @Nullable T value) {
        requestBuilder.tag(cls, value);
    }

    public Request.Builder get() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder;

        if (Utils.isNotEmpty(pathParamMap)) {
            // 存在 path 参数
            for (Map.Entry<String, String> et : pathParamMap.entrySet()) {
                relativeUrl = relativeUrl.replace("{" + et.getKey() + "}", et.getValue());
            }
        }
        if (Utils.isNotEmpty(queryParamMap)) {
            // 带参数的url
            urlBuilder = host.newBuilder(relativeUrl);
            if (urlBuilder == null) {
                throw new IllegalArgumentException("Malformed URL. Host: " + host + ", Relative: " + relativeUrl);
            }
            for (Map.Entry<String, String> et : queryParamMap.entrySet()) {
                urlBuilder.addEncodedQueryParameter(et.getKey(), et.getValue());
            }
            url = urlBuilder.build();
        } else {
            // 不带任何参数的url
            url = host.resolve(relativeUrl);
        }
        if (null != formBuilder && Utils.isNotEmpty(fieldParamMap)) {
            for (Map.Entry<String, String> et : fieldParamMap.entrySet()) {
                formBuilder.addEncoded(et.getKey(), et.getValue());
            }
        }

        if (null == body) {
            if (null != formBuilder) {
                body = formBuilder.build();
            } else if (null != multipartBuilder) {
                body = multipartBuilder.build();
            } else if (hasBody) {
                // 如果强行有body，则设置个空body
                body = RequestBody.create(MediaType.parse("application/json"), new byte[0]);
            }
        }

        if (null != contentType) {
            if (null != body) {
                body = new ContentTypeOverridingRequestBody(body, contentType);
            } else {
                headersBuilder.add("Content-Type", contentType.toString());
            }
        }
        if (url == null) {
            throw new IllegalArgumentException("URL can not be null. Host: " + host + ", Relative: " + relativeUrl);
        }
        return requestBuilder
                .url(url)
                .headers(headersBuilder.build())
                .method(method, body);
    }

    private String dispatchEncode(String value, boolean encoded) {
        try {
            return encoded ? value : URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ContentTypeOverridingRequestBody extends RequestBody {
        private final RequestBody delegate;
        private final MediaType contentType;

        ContentTypeOverridingRequestBody(RequestBody delegate, MediaType contentType) {
            this.delegate = delegate;
            this.contentType = contentType;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() throws IOException {
            return delegate.contentLength();
        }

        @Override
        public void writeTo(@NotNull BufferedSink sink) throws IOException {
            delegate.writeTo(sink);
        }
    }
}
