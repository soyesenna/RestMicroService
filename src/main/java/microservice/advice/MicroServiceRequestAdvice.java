package microservice.advice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import microservice.annotations.MicroServiceIgnore;
import microservice.config.MicroServiceConfig;
import microservice.context.MicroServiceContext;
import microservice.exception.NoClientIDException;
import microservice.exception.NoRequestIDException;
import microservice.templates.MicroServiceRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice
public class MicroServiceRequestAdvice extends RequestBodyAdviceAdapter {

  private final MicroServiceConfig microServiceConfig;

  public MicroServiceRequestAdvice(MicroServiceConfig microServiceConfig) {
    this.microServiceConfig = microServiceConfig;
  }

  @Override
  public boolean supports(MethodParameter methodParameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    Annotation[] methodAnnotations = methodParameter.getMethodAnnotations();
    for (Annotation methodAnnotation : methodAnnotations) {
      if (methodAnnotation.annotationType().equals(MicroServiceIgnore.class)) {
        return false;
      }
    }

    return MicroServiceRequest.class.isAssignableFrom(methodParameter.getParameterType());
  }

  @Override
  public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    MicroServiceRequest<?> microServiceRequest = (MicroServiceRequest<?>) body;

    String requestId = microServiceRequest.requestId();
    String clientId = microServiceRequest.clientId();

    if (requestId == null && clientId != null) {
      throw new NoRequestIDException();
    }

    if (requestId != null && clientId == null) {
      throw new NoClientIDException();
    }

    if (requestId != null) {
      MicroServiceContext.setRequestId(requestId);
    }

    if (clientId != null) {
      MicroServiceContext.setRootClientId(clientId);
    }

    MicroServiceContext.setMyClientId(this.microServiceConfig.getClientId());
    MicroServiceContext.setMetaInfo(microServiceRequest.metadata());

    return microServiceRequest;
  }

  @Override
  public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }
}
