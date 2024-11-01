package microservice.exception;

import java.util.List;
import microservice.constants.Constants;

public class MicroServiceRequestPayloadValidationFailException extends MicroServiceException {

  private final List<String> errorMessages;

  public MicroServiceRequestPayloadValidationFailException(List<String> errorMessages) {
    super(Constants.MICRO_SERVICE_LOG_PREFIX + " Validation failed for payload: "
        + errorMessages, 200);
    this.errorMessages = errorMessages;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }
}
