package io.quarkus.vertx.web.deployment;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.hibernate.validator.spi.BeanValidationAnnotationsBuildItem;
import io.quarkus.vertx.http.runtime.HandlerType;

/**
 * Describe a request handler.
 */
class HandlerDescriptor {

    private final MethodInfo method;
    private final BeanValidationAnnotationsBuildItem validationAnnotations;
    private final HandlerType handlerType;

    HandlerDescriptor(MethodInfo method, BeanValidationAnnotationsBuildItem bvAnnotations, HandlerType handlerType) {
        this.method = method;
        this.validationAnnotations = bvAnnotations;
        this.handlerType = handlerType;
    }

    Type getReturnType() {
        return method.returnType();
    }

    boolean isReturningVoid() {
        return method.returnType().kind().equals(Type.Kind.VOID);
    }

    boolean isReturningUni() {
        return method.returnType().name().equals(DotNames.UNI);
    }

    boolean isReturningMulti() {
        return method.returnType().name().equals(DotNames.MULTI);
    }

    /**
     * @return {@code true} if the method is annotated with a constraint or {@code @Valid} or any parameter has such kind of
     *         annotation.
     */
    boolean requireValidation() {
        if (validationAnnotations == null) {
            return false;
        }
        for (AnnotationInstance annotation : method.annotations()) {
            if (validationAnnotations.getAllAnnotations().contains(annotation.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if the method is annotated with {@code @Valid}.
     */
    boolean isProducedResponseValidated() {
        if (validationAnnotations == null) {
            return false;
        }
        for (AnnotationInstance annotation : method.annotations()) {
            if (validationAnnotations.getValidAnnotation().equals(annotation.name())) {
                return true;
            }
        }
        return false;
    }

    Type getContentType() {
        if (isReturningVoid()) {
            return null;
        }
        if (isReturningUni()) {
            return getReturnType().asParameterizedType().arguments().get(0);
        }
        if (isReturningMulti()) {
            return getReturnType().asParameterizedType().arguments().get(0);
        }
        return getReturnType();
    }

    boolean isContentTypeString() {
        Type type = getContentType();
        if (type == null) {
            return false;
        }
        return type.name().equals(io.quarkus.arc.processor.DotNames.STRING);
    }

    boolean isContentTypeBuffer() {
        Type type = getContentType();
        if (type == null) {
            return false;
        }
        return type.name().equals(DotNames.BUFFER);
    }

    boolean isContentTypeMutinyBuffer() {
        Type type = getContentType();
        if (type == null) {
            return false;
        }
        return type.name().equals(DotNames.MUTINY_BUFFER);
    }

    HandlerType getHandlerType() {
        return handlerType;
    }

}
