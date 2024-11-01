package microservice.exception;

import microservice.constants.Constants;

public class IllegalMicroServiceMethodException extends MicroServiceException {

  public IllegalMicroServiceMethodException(String serviceName) {
    super("In MicroService name [%s], Not annotated with [MicroServiceMethod] method include".formatted(serviceName), 500,
        Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
