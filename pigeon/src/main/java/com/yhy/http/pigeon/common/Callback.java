package com.yhy.http.pigeon.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 17:46
 * version: 1.0.0
 * desc   :
 */
public interface Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable t);
}
