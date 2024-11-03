package microservice.templates;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microservice.context.MicroServiceContext;
import microservice.templates.dtos.ErrorWrapper;

public record MicroServiceResponse<T>(
    String requestId,
    String clientId,
    String timestamp,
    Boolean success,
    T payload,
    List<String> errorStack,
    Integer httpStatusCode,
    Map<String, String> setCookies
) {

  public static void setCookies(Map<String, String> setCookies) {
    MicroServiceContext.setSetCookies(setCookies);
  }

  public static void setHttpStatusCode(Integer httpStatusCode) {
    MicroServiceContext.setHttpStatus(httpStatusCode);
  }

  public static <T> MicroServiceResponse<T> success(T payload, Integer httpStatusCode, Map<String, String> setCookies) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        true,
        payload,
        null,
        httpStatusCode == null ? 200 : httpStatusCode,
        setCookies
    );
  }

  public static MicroServiceResponse<ErrorWrapper> failure(ErrorWrapper errorWrapper) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        false,
        errorWrapper,
        null,
        errorWrapper.httpStatusCode(),
        null
    );
  }

  public static MicroServiceResponse<ErrorWrapper> failure(List<String> errorStack, ErrorWrapper errorWrapper) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        false,
        errorWrapper,
        errorStack,
        errorWrapper.httpStatusCode(),
        null
    );
  }
}