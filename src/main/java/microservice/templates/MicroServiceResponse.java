package microservice.templates;

import java.time.LocalDateTime;
import microservice.config.MicroServiceConfig;
import microservice.context.MicroServiceContext;
import microservice.templates.dtos.ErrorInfo;

public record MicroServiceResponse<T>(
    String requestId,
    String clientId,
    String timestamp,
    Boolean success,
    T payload
) {

  public static <T> MicroServiceResponse<T> success(T payload) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        true,
        payload
    );
  }

  public static MicroServiceResponse<ErrorInfo> failure(ErrorInfo errorInfo) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        false,
        errorInfo
    );
  }

  public static <T> MicroServiceResponse<T> failure(T failPayload) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        false,
        failPayload
    );
  }
}