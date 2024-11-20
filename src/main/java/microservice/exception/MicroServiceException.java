package microservice.exception;

public abstract class MicroServiceException extends RuntimeException{

  private Integer httpStatus;
  private Integer errorCode; // Optional

  protected MicroServiceException(String message, Integer httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  protected MicroServiceException(String message, Integer httpStatus, Integer errorCode) {
    super(message);
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }

  public Integer getHttpStatus() {
    return httpStatus;
  }

  public Integer getErrorCode() {
    return errorCode;
  }
}
