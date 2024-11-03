package microservice.templates;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microservice.context.MicroServiceContext;
import microservice.templates.dtos.ErrorWrapper;
import microservice.templates.dtos.SetCookieWrapper;

public record MicroServiceResponse<T>(
    String requestId,
    String clientId,
    String timestamp,
    Boolean success,
    T payload,
    List<String> errorStack,
    Integer httpStatusCode,
    List<SetCookieWrapper> setCookies
) {

  public static void setCookies(List<SetCookieWrapper> setCookies) {
    MicroServiceContext.setSetCookies(setCookies);
  }

  public static void addCookies(String name, String value, Long maxAge) {
    SetCookieWrapper setCookieWrapper = new SetCookieWrapper(name, value, maxAge);
    MicroServiceContext.addSetCookies(setCookieWrapper);
  }

  public static void setHttpStatusCode(Integer httpStatusCode) {
    MicroServiceContext.setHttpStatus(httpStatusCode);
  }

  public static <T> MicroServiceResponse<T> success(T payload, Integer httpStatusCode, List<SetCookieWrapper> setCookies) {
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