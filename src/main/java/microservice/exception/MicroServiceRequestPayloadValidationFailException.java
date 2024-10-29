package microservice.exception;

import java.util.List;
import microservice.constants.ConstantStrings;

public class MicroServiceRequestPayloadValidationFailException extends MicroServiceException {

  private final List<String> errorMessages;

  public MicroServiceRequestPayloadValidationFailException(List<String> errorMessages) {
    super(ConstantStrings.MICRO_SERVICE_LOG_PREFIX + " Validation failed for payload: "
        + errorMessages);
    this.errorMessages = errorMessages;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }
}
