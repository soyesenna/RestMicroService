package microservice.exception;

import microservice.constants.Constants;

public class NotDefinitionServiceUrlException extends MicroServiceException{

  public NotDefinitionServiceUrlException(String serviceName) {
    super("Not Definition Service Url to " + serviceName, 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
