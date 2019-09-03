package com.yhy.http.pigeon.http.request;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.annotation.*;
import com.yhy.http.pigeon.annotation.method.*;
import com.yhy.http.pigeon.annotation.param.Field;
import com.yhy.http.pigeon.annotation.param.Path;
import com.yhy.http.pigeon.annotation.param.Query;
import com.yhy.http.pigeon.annotation.param.Url;
import com.yhy.http.pigeon.converter.Converter;
import com.yhy.http.pigeon.http.request.param.ParameterHandler;
import com.yhy.http.pigeon.utils.Utils;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 15:59
 * version: 1.0.0
 * desc   :
 */
public class RequestFactory {

    public RequestFactory(Builder builder) {

    }

    public Request create(Object[] args) {
        return null;
    }

    public static RequestFactory parseAnnotations(Pigeon pigeon, Method method) {
        return new Builder(pigeon, method).build();
    }

    private static final class Builder {
        private static final String REGEX_PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        private static final Pattern REGEX_PARAM_URL = Pattern.compile("\\{(" + REGEX_PARAM + ")\\}.*?");
        private static final Pattern REGEX_PARAM_NAME = Pattern.compile(REGEX_PARAM);

        private final Pigeon pigeon;
        private final Method method;
        private final Annotation[] methodAnnotations;
        private final Parameter[] parameters;
        private final Type[] parameterTypes;
        private final Annotation[][] parameterAnnotations;
        private String httpMethod;
        private boolean hasBody;
        private okhttp3.Headers.Builder headersBuilder;
        private okhttp3.Headers headers;
        private MediaType contentType;
        private boolean isForm;
        private boolean isMultipart;
        private String host;
        private String relativeUrl;
        private Set<String> relativeUrlParamNames;
        private ParameterHandler<?>[] parameterHandlers;

        Builder(Pigeon pigeon, Method method) {
            this.pigeon = pigeon;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameters = method.getParameters();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotations = method.getParameterAnnotations();
            this.headersBuilder = new okhttp3.Headers.Builder();
        }

        RequestFactory build() {
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }
            if (httpMethod == null) {
                throw Utils.methodError(method, "HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            }
            if (!hasBody) {
                if (isMultipart) {
                    throw Utils.methodError(method, "Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                }
                if (isForm) {
                    throw Utils.methodError(method, "Form can only be specified on HTTP methods with request body (e.g., @POST).");
                }
            }

            int paramCount = parameters.length;
            parameterHandlers = new ParameterHandler<?>[paramCount];
            for (int i = 0, last = paramCount - 1; i < paramCount; i++) {
                parameterHandlers[i] = parseParameter(i, i == last);
            }

            return new RequestFactory(this);
        }

        @Nullable
        private ParameterHandler<?> parseParameter(int index, boolean last) {
            Parameter parameter = parameters[index];
            Annotation[] annotations = parameter.getAnnotations();
            Type type = parameter.getParameterizedType();

            ParameterHandler<?> handler = null;
            if (null != annotations) {
                for (Annotation annotation : annotations) {
                    if (null != handler) {
                        throw Utils.parameterError(method, index, "Multiple param annotations found, but only one allowed.");
                    }
                    handler = parseParameterAnnotation(index, type, parameter, annotations, annotation);
                }
            } else {
                handler = parseParameterParameter(index, type, parameter);
            }
            if (null == handler && last) {
                throw Utils.parameterError(method, index, "No param annotation found.");
            }
            return handler;
        }

        private ParameterHandler<?> parseParameterParameter(int index, Type type, Parameter parameter) {
            return parseParameterQuery(type, parameter.getName(), false, index, null);
        }

        private ParameterHandler<?> parseParameterAnnotation(int index, Type type, Parameter parameter, Annotation[] annotations, Annotation annotation) {
            validateResolvableType(index, type);
            if (annotation instanceof Url) {
                if (relativeUrl != null) {
                    throw Utils.parameterError(method, index, "@Url cannot be used with @%s URL", httpMethod);
                }
                if (type == String.class || type == HttpUrl.class || type == URI.class) {
                    return new ParameterHandler.RelativeUrl(method, index);
                } else {
                    throw Utils.parameterError(method, index, "@Url must be okhttp3.HttpUrl, String, java.net.URI type.");
                }
            } else if (annotation instanceof Path) {
                if (relativeUrl == null) {
                    throw Utils.parameterError(method, index, "@Path can only be used with relative url on @%s", httpMethod);
                }
                Path path = (Path) annotation;
                String name = path.value();
                if (Utils.isEmpty(name)) {
                    // 如果未指定name，则以参数名称为准
                    name = parameter.getName();
                }
                validatePathName(index, name);
                Converter<?, String> converter = pigeon.stringConverter(type, annotations);
                return new ParameterHandler.Path<>(method, index, name, converter, path.encoded());
            } else if (annotation instanceof Query) {
                Query query = (Query) annotation;
                String name = query.value();
                if (Utils.isEmpty(name)) {
                    name = parameter.getName();
                }
                return parseParameterQuery(type, name, query.encoded(), index, annotations);
            } else if (annotation instanceof Field) {
                if (!isForm) {
                    throw Utils.parameterError(method, index, "@Field parameters can only be used with form encoding.");
                }
                Field field = (Field) annotation;
                String name = field.value();
                if (Utils.isEmpty(name)) {
                    name = parameter.getName();
                }
                boolean encoded = field.encoded();

                Class<?> rawType = Utils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw Utils.parameterError(method, index, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter = pigeon.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Field<>(name, converter, encoded).iterable();
                } else if (rawType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                    Converter<?, String> converter = pigeon.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Field<>(name, converter, encoded).array();
                } else if (Map.class.isAssignableFrom(rawType)) {
                    if (!isForm) {
                        throw Utils.parameterError(method, index, "@Field Map parameters can only be used with form encoding.");
                    }
                    Class<?> rawParameterType = Utils.getRawType(type);
                    if (!Map.class.isAssignableFrom(rawParameterType)) {
                        throw Utils.parameterError(method, index, "@Field Map parameter type must be Map.");
                    }
                    Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                    if (!(mapType instanceof ParameterizedType)) {
                        throw Utils.parameterError(method, index, "Map must include generic types (e.g., Map<String, String>)");
                    }

                    ParameterizedType parameterizedType = (ParameterizedType) mapType;
                    Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                    if (String.class != keyType) {
                        throw Utils.parameterError(method, index, "@Field Map keys must be of type String: " + keyType);
                    }
                    Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                    Converter<?, String> converter = pigeon.stringConverter(valueType, annotations);
                    return new ParameterHandler.FieldMap<>(method, index, converter, encoded);
                } else {
                    Converter<?, String> converter = pigeon.stringConverter(type, annotations);
                    return new ParameterHandler.Field<>(name, converter, encoded);
                }
            } else if (annotation instanceof Header) {
                String name = ((Header) annotation).value();

                Class<?> rawType = Utils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw Utils.parameterError(method, index, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter = pigeon.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Header<>(name, converter).iterable();
                } else if (rawType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                    Converter<?, String> converter = pigeon.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Header<>(name, converter).array();
                } else if (Map.class.isAssignableFrom(rawType)) {
                    if (!isForm) {
                        throw Utils.parameterError(method, index, "@Field Map parameters can only be used with form encoding.");
                    }
                    Class<?> rawParameterType = Utils.getRawType(type);
                    if (!Map.class.isAssignableFrom(rawParameterType)) {
                        throw Utils.parameterError(method, index, "@Field Map parameter type must be Map.");
                    }
                    Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                    if (!(mapType instanceof ParameterizedType)) {
                        throw Utils.parameterError(method, index, "Map must include generic types (e.g., Map<String, String>)");
                    }

                    ParameterizedType parameterizedType = (ParameterizedType) mapType;
                    Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                    if (String.class != keyType) {
                        throw Utils.parameterError(method, index, "@Field Map keys must be of type String: " + keyType);
                    }
                    Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                    Converter<?, String> converter = pigeon.stringConverter(valueType, annotations);
                    return new ParameterHandler.HeaderMap<>(method, index, converter);
                } else {
                    Converter<?, String> converter = pigeon.stringConverter(type, annotations);
                    return new ParameterHandler.Header<>(name, converter);
                }
            }
            return null;
        }

        private ParameterHandler<?> parseParameterQuery(Type type, String name, boolean encoded, int index, Annotation[] annotations) {
            Class<?> rawType = Utils.getRawType(type);
            if (Iterable.class.isAssignableFrom(rawType)) {
                if (!(type instanceof ParameterizedType)) {
                    throw Utils.parameterError(method, index, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                Converter<?, String> converter = pigeon.stringConverter(iterableType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).iterable();
            } else if (rawType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                Converter<?, String> converter = pigeon.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).array();
            } else if (Map.class.isAssignableFrom(rawType)) {
                Type mapType = Utils.getSupertype(type, rawType, Map.class);
                if (!(mapType instanceof ParameterizedType)) {
                    throw Utils.parameterError(method, index, "Map must include generic types (e.g., Map<String, String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) mapType;
                Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw Utils.parameterError(method, index, "@Query Map keys must be of type String: " + keyType);
                }
                Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                Converter<?, String> converter = pigeon.stringConverter(valueType, annotations);
                return new ParameterHandler.QueryMap<>(method, index, converter, encoded);
            } else {
                Converter<?, String> converter = pigeon.stringConverter(type, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded);
            }
        }

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            } else if (annotation instanceof DELETE) {
                parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
            } else if (annotation instanceof HEAD) {
                parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
            } else if (annotation instanceof OPTIONS) {
                parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
            } else if (annotation instanceof PATCH) {
                parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
            } else if (annotation instanceof PUT) {
                parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
            } else if (annotation instanceof TRACE) {
                parseHttpMethodAndPath("TRACE", ((TRACE) annotation).value(), false);
            } else if (annotation instanceof Header) {
                parseHeader((Header) annotation);
            } else if (annotation instanceof Headers) {
                parseHeader(((Headers) annotation).value());
            } else if (annotation instanceof Multipart) {
                if (isForm) {
                    throw Utils.methodError(method, "Only one encoding annotation is allowed.");
                }
                isMultipart = true;
            } else if (annotation instanceof Form) {
                if (isMultipart) {
                    throw Utils.methodError(method, "Only one encoding annotation is allowed.");
                }
                isForm = true;
            } else if (annotation instanceof Host) {
                host = ((Host) annotation).value();
            }
        }

        private void parseHeader(Header... annotation) {
            for (Header header : annotation) {
                String headerName;
                String headerValue;
                // 先检查value
                if (Utils.isNotEmpty(header.value())) {
                    int index = header.value().indexOf(":");
                    if (index <= 0 || ":".equals(header.value())) {
                        throw Utils.methodError(method, "@Header value must be in the form \"Name: Value\". Found: \"%s\"", header.value());
                    }
                    headerName = header.value().substring(0, index).trim();
                    headerValue = header.value().substring(index + 1).trim();
                } else {
                    // 如果value为空，再从pairName和pairValue中获取
                    if (Utils.isEmpty(header.pairName()) || Utils.isEmpty(header.pairValue())) {
                        throw Utils.methodError(method, "@Header pairName and pairValue can not be empty");
                    }
                    headerName = header.pairName();
                    headerValue = header.pairValue();
                }

                if ("Content-Type".equalsIgnoreCase(headerName)) {
                    contentType = MediaType.get(headerValue);
                }
                headersBuilder.add(headerName, headerValue);
            }
            headers = headersBuilder.build();
        }

        private void parseHttpMethodAndPath(String httpMethod, String url, boolean hasBody) {
            if (null != this.httpMethod) {
                throw Utils.methodError(method, "Only one http method is allowed, but found : %s and %s", this.httpMethod, httpMethod);
            }
            this.httpMethod = httpMethod;
            this.hasBody = hasBody;

            if (Utils.isEmpty(url)) {
                return;
            }

            // 如果地址中包含get参数，则参数部分不能含有RESTful参数
            int index = url.indexOf("?");
            if (index > 0 && index < url.length() - 1) {
                String queryParams = url.substring(index + 1);
                if (REGEX_PARAM_URL.matcher(queryParams).find()) {
                    throw Utils.methodError(method, "URL query string \"%s\" must not have replace block. For dynamic query parameters use @Query.", queryParams);
                }
            }
            this.relativeUrl = url;
            this.relativeUrlParamNames = parseUrlParams(url);
        }

        private Set<String> parseUrlParams(String url) {
            Matcher matcher = REGEX_PARAM_URL.matcher(url);
            Set<String> result = new LinkedHashSet<>();
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            return result;
        }

        private void validatePathName(int index, String name) {
            if (!REGEX_PARAM_NAME.matcher(name).matches()) {
                throw Utils.parameterError(method, index, "@Path parameter name must match %s. Found: %s", REGEX_PARAM_URL.pattern(), name);
            }
            if (!relativeUrlParamNames.contains(name)) {
                throw Utils.parameterError(method, index, "URL \"%s\" does not contain \"{%s}\".", relativeUrl, name);
            }
        }

        private void validateResolvableType(int index, Type type) {
            if (Utils.hasUnresolvableType(type)) {
                throw Utils.parameterError(method, index, "Parameter type must not include a type variable or wildcard: %s", type);
            }
        }

        private static Class<?> boxIfPrimitive(Class<?> type) {
            if (boolean.class == type) return Boolean.class;
            if (byte.class == type) return Byte.class;
            if (char.class == type) return Character.class;
            if (double.class == type) return Double.class;
            if (float.class == type) return Float.class;
            if (int.class == type) return Integer.class;
            if (long.class == type) return Long.class;
            if (short.class == type) return Short.class;
            return type;
        }
    }
}
