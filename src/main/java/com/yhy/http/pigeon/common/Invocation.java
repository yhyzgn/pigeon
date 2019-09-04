package com.yhy.http.pigeon.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-04 10:43
 * version: 1.0.0
 * desc   :
 */
public class Invocation {
    private final Method method;
    private final List<?> arguments;

    public Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public Method method() {
        return method;
    }

    public List<?> arguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return String.format("%s.%s() %s", method.getDeclaringClass().getName(), method.getName(), arguments);
    }

    public static Invocation of(Method method, List<?> arguments) {
        Objects.requireNonNull(method, "method can not be null.");
        Objects.requireNonNull(arguments, "arguments can not be null.");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }
}
