package microservice.advice;

import java.lang.annotation.Annotation;
import microservice.annotations.MicroServiceIgnore;
import microservice.config.MicroServiceConfig;
import microservice.templates.MicroServiceResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class MicroServiceResponseAdvice implements ResponseBodyAdvice<Object> {

    private final MicroServiceConfig microServiceConfig;

    public MicroServiceResponseAdvice(MicroServiceConfig microServiceConfig) {
        this.microServiceConfig = microServiceConfig;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation[] methodAnnotations = returnType.getMethodAnnotations();
        for (Annotation methodAnnotation : methodAnnotations) {
            if (methodAnnotation.annotationType().equals(MicroServiceIgnore.class)) {
                return false;
            }
        }

        return !MicroServiceResponse.class.isAssignableFrom(returnType.getParameterType());
    }


    @Override
    public Object beforeBodyWrite(Object body, 
                                MethodParameter returnType,
                                MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request,
                                ServerHttpResponse response) {
        if (body instanceof MicroServiceResponse) {
            return body;
        }

        return MicroServiceResponse.success(body);
    }
}
