package com.yhy.http.pigeon.utils;

import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-02 17:03
 * version: 1.0.0
 * desc   :
 */
public class Utils {
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
    public final static String VERSION = "2.0.8";

    public static boolean isEmpty(Object object) {
        switch (object) {
            case null -> {
                return true;
            }
            case String s -> {
                return s.isEmpty();
            }
            case Collection<?> collection -> {
                return collection.isEmpty();
            }
            case Map<?, ?> map -> {
                return map.isEmpty();
            }
            default -> {
            }
        }
        if (object.getClass().isArray()) {
            return ((Object[]) object).length == 0;
        }
        return false;
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException("Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    public static Type getParameterLowerBound(int index, ParameterizedType type) {
        Type paramType = type.getActualTypeArguments()[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getLowerBounds()[0];
        }
        return paramType;
    }

    public static boolean hasUnresolvableType(@Nullable Type type) {
        if (type instanceof Class<?>) {
            return false;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(typeArgument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return false;
        }
        if (type instanceof WildcardType) {
            return false;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
    }

    public static Class<?> getRawType(Type type) {
        Objects.requireNonNull(type, "type can not be null.");

        switch (type) {
            case Class<?> aClass -> {
                // Type is a normal class.
                return aClass;
                // Type is a normal class.
            }
            case ParameterizedType parameterizedType -> {
                // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
                // suspects some pathological case related to nested classes exists.
                Type rawType = parameterizedType.getRawType();
                if (!(rawType instanceof Class)) throw new IllegalArgumentException();
                return (Class<?>) rawType;
            }
            case GenericArrayType genericArrayType -> {
                Type componentType = genericArrayType.getGenericComponentType();
                return Array.newInstance(getRawType(componentType), 0).getClass();
            }
            case TypeVariable<?> typeVariable -> {
                // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
                // type that's more general than necessary is okay.
                return Object.class;
                // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
                // type that's more general than necessary is okay.
            }
            case WildcardType wildcardType -> {
                return getRawType(wildcardType.getUpperBounds()[0]);
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    public static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        if (!supertype.isAssignableFrom(contextRawType)) throw new IllegalArgumentException();
        return resolve(context, contextRawType, getGenericSupertype(context, contextRawType, supertype));
    }

    public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        // This implementation is made a little more complicated in an attempt to avoid object-creation.
        while (true) {
            switch (toResolve) {
                case TypeVariable<?> typeVariable -> {
                    toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
                    if (toResolve == typeVariable) {
                        return toResolve;
                    }
                }
                case Class<?> original when original.isArray() -> {
                    Type componentType = original.getComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                            newComponentType);

                }
                case GenericArrayType original -> {
                    Type componentType = original.getGenericComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                            newComponentType);

                }
                case ParameterizedType original -> {
                    Type ownerType = original.getOwnerType();
                    Type newOwnerType = resolve(context, contextRawType, ownerType);
                    boolean changed = newOwnerType != ownerType;

                    Type[] args = original.getActualTypeArguments();
                    for (int t = 0, length = args.length; t < length; t++) {
                        Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
                        if (resolvedTypeArgument != args[t]) {
                            if (!changed) {
                                args = args.clone();
                                changed = true;
                            }
                            args[t] = resolvedTypeArgument;
                        }
                    }

                    return changed
                            ? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
                            : original;
                }
                case WildcardType original -> {
                    Type[] originalLowerBound = original.getLowerBounds();
                    Type[] originalUpperBound = original.getUpperBounds();

                    if (originalLowerBound.length == 1) {
                        Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
                        if (lowerBound != originalLowerBound[0]) {
                            return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
                        }
                    } else if (originalUpperBound.length == 1) {
                        Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
                        if (upperBound != originalUpperBound[0]) {
                            return new WildcardTypeImpl(new Type[]{upperBound}, EMPTY_TYPE_ARRAY);
                        }
                    }
                    return original;
                }
                case null, default -> {
                    return toResolve;
                }
            }
        }
    }

    public static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(method, null, message, args);
    }

    public static RuntimeException methodError(Method method, @Nullable Throwable cause, String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException(message
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName(), cause);
    }

    public static RuntimeException parameterError(Method method, Throwable cause, int p, String message, Object... args) {
        return methodError(method, cause, message + " (parameter #" + (p + 1) + ")", args);
    }

    public static RuntimeException parameterError(Method method, int p, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (p + 1) + ")", args);
    }

    public static ResponseBody buffer(ResponseBody rawBody) throws IOException {
        Buffer buffer = new Buffer();
        rawBody.source().readAll(buffer);
        return ResponseBody.create(buffer, rawBody.contentType(), rawBody.contentLength());
    }

    /**
     * 判断处理配置变量 ${xxx.xxx}
     *
     * @param value 配置值
     * @return true 表示存在置变量，false 表示不存在
     */
    public static boolean isPlaceholdersPresent(String value) {
        return !isEmpty(value) && Pattern.matches(".*?\\$\\{\\s*[0-9a-zA-Z\\-_.]+\\s*?}.*", value);
    }

    private static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // We can't reduce this further.
        if (declaredByRaw == null) return unknown;

        Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) return context;

        // We skip searching through interfaces if unknown is an interface.
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // Check our supertypes.
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // We can't resolve this further.
        return toResolve;
    }

    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) {
            if (toFind.equals(array[i])) return i;
        }
        throw new NoSuchElementException();
    }

    static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    static void checkNotPrimitive(Type type) {
        if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
            throw new IllegalArgumentException();
        }
    }

    static boolean isAnnotationPresent(Annotation[] annotations, Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    static boolean equals(Type a, Type b) {
        if (a == b) {
            return true; // Also handles (a == null && b == null).
        } else if (a instanceof Class) {
            return a.equals(b); // Class already specifies equals().
        } else if (a instanceof ParameterizedType pa) {
            if (!(b instanceof ParameterizedType pb)) return false;
            Object ownerA = pa.getOwnerType();
            Object ownerB = pb.getOwnerType();
            return (Objects.equals(ownerA, ownerB)) && pa.getRawType().equals(pb.getRawType()) && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
        } else if (a instanceof GenericArrayType ga) {
            if (!(b instanceof GenericArrayType gb)) return false;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
        } else if (a instanceof WildcardType wa) {
            if (!(b instanceof WildcardType wb)) return false;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
        } else if (a instanceof TypeVariable<?> va) {
            if (!(b instanceof TypeVariable<?> vb)) return false;
            return va.getGenericDeclaration() == vb.getGenericDeclaration() && va.getName().equals(vb.getName());
        } else {
            return false; // This isn't a type we support!
        }
    }

    static final class ParameterizedTypeImpl implements ParameterizedType {
        private final @Nullable
        Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... typeArguments) {
            // Require an owner type if the raw type needs it.
            if (rawType instanceof Class<?>
                    && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
                throw new IllegalArgumentException();
            }

            for (Type typeArgument : typeArguments) {
                Objects.requireNonNull(typeArgument, "typeArgument == null");
                checkNotPrimitive(typeArgument);
            }

            this.ownerType = ownerType;
            this.rawType = rawType;
            this.typeArguments = typeArguments.clone();
        }

        @Override
        public Type @NotNull [] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public @NotNull Type getRawType() {
            return rawType;
        }

        @Override
        public @Nullable
        Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && Utils.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments)
                    ^ rawType.hashCode()
                    ^ (ownerType != null ? ownerType.hashCode() : 0);
        }

        @Override
        public String toString() {
            if (typeArguments.length == 0) return typeToString(rawType);
            StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
            result.append(typeToString(rawType));
            result.append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < typeArguments.length; i++) {
                result.append(", ").append(typeToString(typeArguments[i]));
            }
            return result.append(">").toString();
        }
    }

    private record GenericArrayTypeImpl(Type componentType) implements GenericArrayType {

        @Override
        public @NotNull Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType && Utils.equals(this, (GenericArrayType) o);
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type upperBound;
        private final @Nullable
        Type lowerBound;

        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            if (lowerBounds.length > 1) throw new IllegalArgumentException();
            if (upperBounds.length != 1) throw new IllegalArgumentException();

            if (lowerBounds.length == 1) {
                if (lowerBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(lowerBounds[0]);
                if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
                this.lowerBound = lowerBounds[0];
                this.upperBound = Object.class;
            } else {
                if (upperBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = upperBounds[0];
            }
        }

        @Override
        public Type @NotNull [] getUpperBounds() {
            return new Type[]{upperBound};
        }

        @Override
        public Type @NotNull [] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && Utils.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) return "? super " + typeToString(lowerBound);
            if (upperBound == Object.class) return "?";
            return "? extends " + typeToString(upperBound);
        }
    }
}
