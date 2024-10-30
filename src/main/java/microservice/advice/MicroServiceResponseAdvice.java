package microservice.advice;

import microservice.config.MicroServiceConfig;
import microservice.constants.ConstantStrings;
import microservice.context.MicroServiceContext;
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
        if (MicroServiceContext.isContextEmpty()) {
            return false;
        }

        boolean isClient = MicroServiceContext.getClientId().equals(ConstantStrings.API_CLIENT_ID);

        boolean isSupport = false;

        if (!isClient) {
            isSupport = !MicroServiceResponse.class.isAssignableFrom(returnType.getParameterType());
        }

        return isSupport;
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

        return MicroServiceResponse.success(body, microServiceConfig);
    }
}
