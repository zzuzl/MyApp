package cn.zlihj.mybatis;

import java.lang.reflect.Method;

public final class ConvertableContext<S, T> {
    private final Class<S> sourceType;
    private final Class<T> targetType;
    private final Method valueMethod;
    private final Method ofMethod;

    private final Object[] zeroParameter = new Object[0];

    ConvertableContext(final Class<S> sourceType,
            final Class<T> targetType,
            final Method valueMethod,
            final Method ofMethod) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.valueMethod = valueMethod;
        this.ofMethod = ofMethod;
    }

    public Class<S> getSourceType() {
        return sourceType;
    }

    public Class<T> getValueType() {
        return targetType;
    }

    public T value(final S source) throws Exception {
        return targetType.cast(valueMethod.invoke(source, zeroParameter));
    }

    public S of(final T target) throws Exception {
        return sourceType.cast(ofMethod.invoke(null, target));
    }

    @SuppressWarnings("unchecked")
    public static <S, T> ConvertableContext<S, T> build(final String className)
            throws RuntimeException {
        Class<S> convertableClazz;
        try {
            convertableClazz = (Class<S>) Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Class not found", cnfe);
        }
        return build(convertableClazz);
    }

    public static <S, T> ConvertableContext<S, T> build(final Class<S> srcType)
            throws RuntimeException {

        if (!srcType.isAnnotationPresent(Convertable.class)) {
            throw new RuntimeException(
                    "Class should be annotated by Convertable.");
        }

        Convertable convertable = srcType
                .getAnnotation(Convertable.class);
        Method valueMethod;
        try {
            valueMethod = srcType.getMethod(convertable.valueMethod(),
                    new Class[0]);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to obtain method:"
                    + convertable.valueMethod()
                    + "(which should have non-argument).",
                    exception);
        }

        @SuppressWarnings("unchecked")
        Class<T> targetType = (Class<T>) valueMethod.getReturnType();

        Method ofMethod;
        try {
            ofMethod = srcType.getMethod(convertable.ofMethod(),
                    new Class[] { targetType });
        } catch (Exception exception) {
            throw new RuntimeException("Failed to obtain method:"
                    + convertable.ofMethod() + "(which should have a(n) "
                    + targetType.getName() + " typed argument)", exception);
        }

        return new ConvertableContext<S, T>(srcType, targetType, valueMethod,
                ofMethod);
    }
}
