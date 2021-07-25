package pigeon.header;

import com.yhy.http.pigeon.annotation.Header;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-10-26 21:02
 * version: 1.0.0
 * desc   :
 */
public class TimestampHeader implements Header.Dynamic {

    @Override
    public Map<String, String> pairs(Method method) {
        Map<String, String> mp = new HashMap<>();
        mp.put("Timestamp", new Date().getTime() + "");
        return mp;
    }
}
