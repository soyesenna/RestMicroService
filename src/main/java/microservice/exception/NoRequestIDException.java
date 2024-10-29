package microservice.exception;

public class NoRequestIDException extends MicroServiceException{

  public NoRequestIDException() {
    super("Can NOT find Request ID");
  }
}
