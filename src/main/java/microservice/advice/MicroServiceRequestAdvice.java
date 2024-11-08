package microservice.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import microservice.annotations.MicroServiceIgnore;
import microservice.config.MicroServiceConfig;
import microservice.constants.Constants;
import microservice.context.MicroServiceContext;
import microservice.exception.MicroServiceRequestPayloadValidationFailException;
import microservice.exception.NoClientIDException;
import microservice.exception.NoRequestIDException;
import microservice.templates.MicroServiceRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice
public class MicroServiceRequestAdvice extends RequestBodyAdviceAdapter {

  private static final Log log = LogFactory.getLog(MicroServiceRequestAdvice.class);


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

    MicroServiceContext.setHttpStatus(200);
    MicroServiceContext.setMyClientId(this.microServiceConfig.getClientId());
    MicroServiceContext.setMetaInfo(microServiceRequest.metadata());

    log.info("%s MicroServiceRequest -> %s".formatted(Constants.MICRO_SERVICE_LOG_PREFIX,
        microServiceRequest.toString()));

    this.validateRequest(microServiceRequest);

    return microServiceRequest;
  }

  private <T> void validateRequest(MicroServiceRequest<T> microServiceRequest) {
    T payload = microServiceRequest.payload();
    if (payload != null) {
      Validator validator;
      try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
        validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(payload);

        if (!violations.isEmpty()) {
          List<String> errorMessages = violations.stream()
              .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
              .collect(Collectors.toList());

          throw new MicroServiceRequestPayloadValidationFailException(errorMessages);
        }
      }
    }
  }

  @Override
  public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }
}
