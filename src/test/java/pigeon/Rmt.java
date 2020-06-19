package pigeon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-06-19 11:03
 * version: 1.0.0
 * desc   :
 */
public class Rmt<T> {
    // Success 成功
    public final static int Success = 0;
    // Failure 失败
    public final static int Failure = 1;

    private int ret;

    private T data;

    private String msg;

    @JsonProperty("errorcode")
    @SerializedName("errorcode")
    private int errorCode;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "Rmt{" +
                "ret=" + ret +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }

    /**
     * 成功标准判断
     *
     * @return 是否成功
     */
    public boolean ok() {
        return ret == Success && errorCode == Success;
    }

    /**
     * 失败乃成功之母
     *
     * @return 不成功便成仁
     */
    public boolean fail() {
        return !ok();
    }
}
