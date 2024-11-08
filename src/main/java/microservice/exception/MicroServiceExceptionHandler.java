package microservice.exception;

import java.util.ArrayList;
import java.util.List;
import microservice.context.MicroServiceContext;
import microservice.templates.MicroServiceResponse;
import microservice.templates.dtos.ErrorWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(1)
public class MicroServiceExceptionHandler {

  private static final Log log = LogFactory.getLog(MicroServiceExceptionHandler.class);

  @ExceptionHandler(value = MicroServiceRequestFailException.class)
  @ResponseStatus(HttpStatus.OK)
  public MicroServiceResponse<ErrorWrapper> handleMicroServiceRequestFailException(
      MicroServiceRequestFailException e) {
    List<String> errorStack;
    if (e.getErrorStack() == null) {
      errorStack = new ArrayList<>();
    } else {
      errorStack = e.getErrorStack();
    }

    errorStack.add(
        "In %s to %s : %s %s Request Failed".formatted(MicroServiceContext.getMyClientId(),
            e.getServiceName(),
            e.getHttpMethod().toString(), e.getRequestUrl()));

    return MicroServiceResponse.failure(errorStack, e.getErrorInfo());
  }

  @ExceptionHandler(value = MicroServiceException.class)
  @ResponseStatus(HttpStatus.OK)
  public MicroServiceResponse<ErrorWrapper> handleMicroServiceException(MicroServiceException e) {
    log.info("MicroServiceException : " + e.getMessage());
    ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(), e.getHttpStatus(),
        e.getErrorCode());
    return MicroServiceResponse.failure(errorWrapper);
  }

  @ExceptionHandler(value = MicroServiceRequestPayloadValidationFailException.class)
  @ResponseStatus(HttpStatus.OK)
  public MicroServiceResponse<ErrorWrapper> handleMicroServiceRequestPayloadValidationFailException(
      MicroServiceRequestPayloadValidationFailException e) {
    log.info("MicroServiceRequestPayloadValidationFailException : " + e.getMessage());
    ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(), e.getHttpStatus(),
        e.getErrorCode());
    return MicroServiceResponse.failure(errorWrapper);
  }
}
