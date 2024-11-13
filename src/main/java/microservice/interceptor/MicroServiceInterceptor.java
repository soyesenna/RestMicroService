package microservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import microservice.annotations.MicroServiceController;
import microservice.config.MicroServiceConfig;
import microservice.constants.Constants;
import microservice.context.MicroServiceContext;
import microservice.exception.MicroServiceRequestPayloadValidationFailException;
import microservice.exception.NoClientIDException;
import microservice.exception.NoRequestIDException;
import microservice.templates.MicroServiceRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MicroServiceInterceptor implements HandlerInterceptor {

  private static final Log log = LogFactory.getLog(MicroServiceInterceptor.class);

  private final MicroServiceConfig microServiceConfig;

  public MicroServiceInterceptor(MicroServiceConfig microServiceConfig) {
    this.microServiceConfig = microServiceConfig;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      if (handlerMethod.getBean().getClass().isAnnotationPresent(MicroServiceController.class)) {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("%s Initializing MicroServiceContext".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));
        MicroServiceContext.init();

        String body = request.getReader().lines()
            .collect(Collectors.joining(System.lineSeparator()));
        MicroServiceRequest<?> microServiceRequest = objectMapper.readValue(body,
            MicroServiceRequest.class);

        this.validateRequest(microServiceRequest);

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
      }
    }
    return true;
  }

  private <T> void validateRequest(MicroServiceRequest<T> microServiceRequest) {
    T payload = microServiceRequest.payload();
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
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    log.info("%s Clearing MicroServiceContext".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));
    MicroServiceContext.clear();
  }
}
