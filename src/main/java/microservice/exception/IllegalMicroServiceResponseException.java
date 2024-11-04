package microservice.exception;

public class IllegalMicroServiceResponseException extends MicroServiceException {

  public IllegalMicroServiceResponseException(String message, Integer httpStatus) {
    super(message, httpStatus);
  }
}
