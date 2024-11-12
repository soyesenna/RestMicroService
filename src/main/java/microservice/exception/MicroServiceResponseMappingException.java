package microservice.exception;

import microservice.constants.Constants;

public class MicroServiceResponseMappingException extends MicroServiceException{

  public MicroServiceResponseMappingException(String clientId) {
    super("In MicroService [%s] response mapping error".formatted(clientId), 500,
        Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
