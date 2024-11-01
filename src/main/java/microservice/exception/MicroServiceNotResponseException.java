package microservice.exception;

import microservice.constants.Constants;

public class MicroServiceNotResponseException extends MicroServiceException{

  public MicroServiceNotResponseException(String url) {
    super("Empty Response in " + url, 500, Constants.MICRO_SERVICE_ERROR_CODE);
  }
}
