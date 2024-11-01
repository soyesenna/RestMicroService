package microservice.templates;

import java.time.LocalDateTime;
import java.util.List;
import microservice.context.MicroServiceContext;
import microservice.templates.dtos.ErrorWrapper;

public record MicroServiceResponse<T>(
    String requestId,
    String clientId,
    String timestamp,
    Boolean success,
    T payload,
    List<String> errorStack
) {

  public static <T> MicroServiceResponse<T> success(T payload) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        true,
        payload,
        null
    );
  }

  public static MicroServiceResponse<ErrorWrapper> failure(ErrorWrapper errorWrapper) {
    return new MicroServiceResponse<>(
        MicroServiceContext.getRequestId(),
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        false,
        errorWrapper,
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
        errorStack
    );
  }
}