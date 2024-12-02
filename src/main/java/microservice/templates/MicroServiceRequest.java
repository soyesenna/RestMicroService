package microservice.templates;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import microservice.context.MicroServiceContext;

public record MicroServiceRequest<T>(
    String requestId,
    String clientId,
    String timestamp,
    @Valid
    T payload,
    Map<String, Object> metadata
) {

  public static <T> MicroServiceRequest<T> createRequest(String requestId, T payload) {
    return new MicroServiceRequest<>(
        requestId,
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        payload,
        MicroServiceContext.getMetaInfo()
    );
  }

  public static <T> MicroServiceRequest<T> createRequest(String requestId, T payload,
      Map<String, Object> metadata) {
    return new MicroServiceRequest<>(
        requestId,
        MicroServiceContext.getMyClientId(),
        LocalDateTime.now().toString(),
        payload,
        metadata
    );
  }
}