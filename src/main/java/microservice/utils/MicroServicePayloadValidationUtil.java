package microservice.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import microservice.exception.MicroServiceRequestPayloadValidationFailException;
import org.springframework.stereotype.Component;

@Component
public class MicroServicePayloadValidationUtil {

  public void validatePayload(Object payload) {
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
}
