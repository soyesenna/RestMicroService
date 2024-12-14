package microservice.utils;

import microservice.config.MicroServiceConfig;
import microservice.constants.Constants;
import microservice.context.MicroServiceContext;
import microservice.exception.NoClientIDException;
import microservice.exception.NoRequestIDException;
import microservice.templates.MicroServiceRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class MicroServiceContextInitUtil {

  private static final Log log = LogFactory.getLog(MicroServiceContextInitUtil.class);
  private final MicroServiceConfig microServiceConfig;

  public MicroServiceContextInitUtil(MicroServiceConfig microServiceConfig) {
    this.microServiceConfig = microServiceConfig;
  }

  public void init(MicroServiceRequest<?> microServiceRequest) {
    log.info("%s Initializing MicroServiceContext".formatted(Constants.MICRO_SERVICE_START_REQUEST_LOG_PREFIX));
    log.info("%s MicroServiceRequest -> %s".formatted(Constants.MICRO_SERVICE_LOG_PREFIX,
        microServiceRequest.toString()));
    MicroServiceContext.init();

    String requestId = microServiceRequest.requestId();
    String clientId = microServiceRequest.clientId();

    if (requestId == null && clientId != null) {
      throw new NoRequestIDException();
    }

    if (requestId != null && clientId == null) {
      throw new NoClientIDException();
    }

    if (requestId != null) {
      MicroServiceContext.setRequestId(requestId);
    }

    if (clientId != null) {
      MicroServiceContext.setRootClientId(clientId);
    }

    MicroServiceContext.setHttpStatus(200);
    MicroServiceContext.setMyClientId(this.microServiceConfig.getClientId());
    MicroServiceContext.setMetaInfo(microServiceRequest.metadata());

    log.info("%s MicroServiceRequest initialized".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));
  }
}
