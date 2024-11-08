package microservice.exception;

import java.util.List;
import microservice.constants.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MicroServiceRequestPayloadValidationFailException extends MicroServiceException {

  private static final Log log = LogFactory.getLog(
      MicroServiceRequestPayloadValidationFailException.class);
  private final List<String> errorMessages;

  public MicroServiceRequestPayloadValidationFailException(List<String> errorMessages) {
    super(errorMessages.toString(), 200);
    log.error(Constants.MICRO_SERVICE_LOG_PREFIX + " Validation failed for payload: "
        + errorMessages);
    this.errorMessages = errorMessages;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }
}
