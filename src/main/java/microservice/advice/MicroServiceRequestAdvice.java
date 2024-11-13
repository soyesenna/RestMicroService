package microservice.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import microservice.annotations.MicroServiceController;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice(annotations = MicroServiceController.class)
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
        log.info("%s MicroServiceIgnore annotation found, skipping MicroServiceRequestAdvice".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));
        return false;
      }
    }

    log.info("%s MicroServiceRequestAdvice supports to type -> [%s]".formatted(Constants.MICRO_SERVICE_LOG_PREFIX, targetType.getTypeName()));
    return true;
  }

  @Override
  public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
      Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    MicroServiceRequest<?> microServiceRequest = objectMapper.readValue(inputMessage.getBody(),
        MicroServiceRequest.class);

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

    return new HttpInputMessage() {
      @Override
      public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(microServiceRequest.payload()));
      }

      @Override
      public HttpHeaders getHeaders() {
        return inputMessage.getHeaders();
      }
    };
  }

  @Override
  public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
      Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
    this.validateRequest(body);
    return body;
  }

  private <T> void validateRequest(Object payload) {
    if (payload != null) {
      Validator validator;
      try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
        validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(payload);

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
