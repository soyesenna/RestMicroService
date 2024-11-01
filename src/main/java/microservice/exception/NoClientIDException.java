package microservice.exception;

public class NoClientIDException extends MicroServiceException {

  public NoClientIDException() {
    super("Can NOT find Request ID", 500);
  }
}

