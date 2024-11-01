package microservice.exception;

import microservice.constants.Constants;

public class MicroServiceCreateException extends MicroServiceException {

  public MicroServiceCreateException(String message) {
    super(message, 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
