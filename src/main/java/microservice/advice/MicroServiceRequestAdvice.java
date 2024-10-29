package microservice.advice;

import java.lang.reflect.Type;
import java.util.UUID;
import microservice.constants.ConstantStrings;
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

  @Override
  public boolean supports(MethodParameter methodParameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
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
      MicroServiceContext.setClientId(clientId);
    }

    if (requestId == null) {
      String firstRequestId = UUID.randomUUID().toString();
      MicroServiceContext.setRequestId(firstRequestId);
      MicroServiceContext.setClientId(ConstantStrings.API_CLIENT_ID);

      microServiceRequest = new MicroServiceRequest<>(
          firstRequestId,
          ConstantStrings.API_CLIENT_ID,
          microServiceRequest.timestamp(),
          microServiceRequest.payload(),
          microServiceRequest.metadata()
      );
    }

    return microServiceRequest;
  }

  @Override
  public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }
}
