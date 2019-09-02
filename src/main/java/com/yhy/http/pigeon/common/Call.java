package com.yhy.http.pigeon.common;

import okhttp3.Request;

import java.io.IOException;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 17:44
 * version: 1.0.0
 * desc   :
 */
public interface Call<T> extends Cloneable {

    Response<T> execute() throws IOException;

    void enqueue(Callback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

    Request request();
}
