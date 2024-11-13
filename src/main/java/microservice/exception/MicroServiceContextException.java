package microservice.exception;

import microservice.constants.Constants;

public class MicroServiceContextException extends MicroServiceException{

  public MicroServiceContextException(String emptyContextName) {
    super("MicroService Context name [%s] is empty".formatted(emptyContextName), 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
