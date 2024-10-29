package microservice.exception;

public abstract class MicroServiceException extends RuntimeException{

  protected MicroServiceException(String message) {
    super(message);
  }
}
