package microservice.exception;

public class IllegalMicroServiceMethodException extends MicroServiceException {

  public IllegalMicroServiceMethodException(String serviceName) {
    super("In MicroService name [%s], Not annotated with [MicroServiceMethod] method include".formatted(serviceName), 500);
  }
}
