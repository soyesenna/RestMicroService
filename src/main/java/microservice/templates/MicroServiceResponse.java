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

  public static <T> MicroServiceResponse<T> success(T payload, MicroServiceConfig microServiceConfig) {
    String clientId = microServiceConfig.getClientId();
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        clientId,
        LocalDateTime.now().toString(),
        true,
        payload
    );
  }

  public static MicroServiceResponse<ErrorInfo> failure(ErrorInfo errorInfo, MicroServiceConfig microServiceConfig) {
    String clientId = microServiceConfig.getClientId();
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        clientId,
        LocalDateTime.now().toString(),
        false,
        errorInfo
    );
  }

  public static <T> MicroServiceResponse<T> failure(T failPayload, MicroServiceConfig microServiceConfig) {
    String clientId = microServiceConfig.getClientId();
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        clientId,
        LocalDateTime.now().toString(),
        false,
        failPayload
    );
  }
}