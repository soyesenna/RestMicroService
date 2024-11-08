package microservice.templates;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import microservice.context.MicroServiceContext;
import microservice.exception.MicroServiceRequestPayloadValidationFailException;

public record MicroServiceRequest<T>(
    String requestId,
    String clientId,
    String timestamp,
    @Valid
    T payload,
    Map<String, Object> metadata
) {

  public MicroServiceRequest {
//    if (payload != null) {
//      Validator validator;
//      try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
//        validator = factory.getValidator();
//        Set<ConstraintViolation<T>> violations = validator.validate(payload);
//
//        if (!violations.isEmpty()) {
//          List<String> errorMessages = violations.stream()
//              .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
//              .collect(Collectors.toList());
//
//          throw new MicroServiceRequestPayloadValidationFailException(errorMessages);
//        }
//      }
//    }
  }

  public static <T> MicroServiceRequest<T> createRequest(String requestId, T payload) {
    return new MicroServiceRequest<>(
        requestId,
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        payload,
        MicroServiceContext.getMetaInfo()
    );
  }

  public static  <T> MicroServiceRequest<T> createRequest(String requestId, T payload, Map<String, Object> metadata) {
    return new MicroServiceRequest<>(
        requestId,
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        payload,
        metadata
    );
  }
}