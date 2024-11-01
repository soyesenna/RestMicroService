package microservice.exception;

import java.util.ArrayList;
import java.util.List;
import microservice.context.MicroServiceContext;
import microservice.templates.MicroServiceResponse;
import microservice.templates.dtos.ErrorWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MicroServiceExceptionHandler {

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

    errorStack.add("In %s to %s : %s %s Request Failed".formatted(MicroServiceContext.getMyClientId(), e.getServiceName(),
        e.getHttpMethod().toString(), e.getRequestUrl()));

    return MicroServiceResponse.failure(errorStack, e.getErrorInfo());
  }

  @ExceptionHandler(value = MicroServiceException.class)
  @ResponseStatus(HttpStatus.OK)
  public MicroServiceResponse<ErrorWrapper> handleMicroServiceException(MicroServiceException e) {
    ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(), e.getHttpStatus(), e.getErrorCode());
    return MicroServiceResponse.failure(errorWrapper);
  }
}
