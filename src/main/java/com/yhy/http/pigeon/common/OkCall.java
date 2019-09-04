package com.yhy.http.pigeon.common;

import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.http.request.RequestFactory;
import com.yhy.http.pigeon.utils.Utils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 10:03
 * version: 1.0.0
 * desc   :
 */
public class OkCall<T> implements Call<T> {
    private final RequestFactory requestFactory;
    private final OkHttpClient.Builder client;
    private final Converter<ResponseBody, T> responseConverter;
    private final Object[] args;

    private volatile boolean canceled;
    @Nullable
    private okhttp3.Call rawCall;
    @Nullable
    private Throwable failureHandler;
    private boolean executed;

    public OkCall(RequestFactory requestFactory, OkHttpClient.Builder client, Converter<ResponseBody, T> responseConverter, Object[] args) {
        this.requestFactory = requestFactory;
        this.client = client;
        this.responseConverter = responseConverter;
        this.args = args;
    }

    @Override
    public Response<T> execute() throws IOException {
        okhttp3.Call call;
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            if (failureHandler != null) {
                if (failureHandler instanceof IOException) {
                    throw (IOException) failureHandler;
                } else if (failureHandler instanceof RuntimeException) {
                    throw (RuntimeException) failureHandler;
                } else {
                    throw (Error) failureHandler;
                }
            }
            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (RuntimeException | Error e) {
                    failureHandler = e;
                    throw e;
                }
            }
        }
        if (canceled) {
            call.cancel();
        }
        return parseResponse(call.execute());
    }

    @Override
    public synchronized Request request() {
        okhttp3.Call call = rawCall;
        if (null != call) {
            return call.request();
        }
        if (failureHandler != null) {
            if (failureHandler instanceof IOException) {
                throw new RuntimeException("Unable to create request.", failureHandler);
            } else if (failureHandler instanceof RuntimeException) {
                throw (RuntimeException) failureHandler;
            } else {
                throw (Error) failureHandler;
            }
        }
        try {
            call = rawCall = createRawCall();
            return call.request();
        } catch (RuntimeException | Error e) {
            failureHandler = e;
            throw e;
        }
    }

    @Override
    public void enqueue(Callback<T> callback) {
        Objects.requireNonNull(callback, "callback can not be null.");
        okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            failure = failureHandler;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    failure = failureHandler = t;
                }
            }
        }
        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }
        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                callFailure(e);
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response rawResponse) throws IOException {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }
                try {
                    callback.onResponse(OkCall.this, response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkCall.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;
        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public OkCall<T> clone() {
        return new OkCall<>(requestFactory, client, responseConverter, args);
    }

    private okhttp3.Call createRawCall() {
        Request request = null;
        try {
            request = requestFactory.create(client, args);
            return client.build().newCall(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
        try {
            T body = responseConverter.convert(catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    static final class NoContentResponseBody extends ResponseBody {
        @Nullable
        private final MediaType contentType;
        private final long contentLength;

        NoContentResponseBody(@Nullable MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    static final class ExceptionCatchingResponseBody extends ResponseBody {
        private final ResponseBody delegate;
        private final BufferedSource delegateSource;
        @Nullable
        IOException thrownException;

        ExceptionCatchingResponseBody(ResponseBody delegate) {
            this.delegate = delegate;
            this.delegateSource = Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return delegateSource;
        }

        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
