package microservice.exception;

public class NotDefinitionServiceUrlException extends MicroServiceException{

  public NotDefinitionServiceUrlException(String serviceName) {
    super("Not Definition Service Url to " + serviceName);
  }
}
