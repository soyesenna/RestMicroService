package microservice.exception;

import microservice.constants.Constants;

public class NoClientIDException extends MicroServiceException {

  public NoClientIDException() {
    super("Can NOT find Request ID", 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}

