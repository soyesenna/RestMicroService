package microservice.exception;

import microservice.constants.Constants;

public class NoRequestIDException extends MicroServiceException{

  public NoRequestIDException() {
    super("Can NOT find Request ID", 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
