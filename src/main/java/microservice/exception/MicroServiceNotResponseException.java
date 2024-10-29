package microservice.exception;

public class MicroServiceNotResponseException extends MicroServiceException{

  public MicroServiceNotResponseException(String url) {
    super("Empty Response in " + url);
  }
}
