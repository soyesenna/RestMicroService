package microservice.exception;

public abstract class MicroServiceException extends RuntimeException{

  private Integer httpStatus;

  protected MicroServiceException(String message, Integer httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public Integer getHttpStatus() {
    return httpStatus;
  }
}
