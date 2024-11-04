package microservice.invocation_handler;

import static microservice.constants.Constants.MICRO_SERVICE_LOG_PREFIX;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import microservice.annotations.MicroServiceMethod;
import microservice.config.MicroServiceConfig;
import microservice.context.MicroServiceContext;
import microservice.exception.IllegalMicroServiceMethodException;
import microservice.exception.IllegalMicroServiceResponseException;
import microservice.exception.MicroServiceNotResponseException;
import microservice.exception.MicroServiceRequestFailException;
import microservice.exception.NotDefinitionServiceUrlException;
import microservice.templates.MicroServiceRequest;
import microservice.templates.MicroServiceResponse;
import microservice.templates.dtos.ErrorWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

public class MicroServiceInvocationHandler implements InvocationHandler {

  private static final Log log = LogFactory.getLog(MicroServiceInvocationHandler.class);
  private final String serviceName;
  private final RestClient restClient;
  private final Class<?> serviceInterface;
  private final MicroServiceConfig microServiceConfig;
  private final ObjectMapper objectMapper;

  public MicroServiceInvocationHandler(String serviceName, RestClient restClient,
      Class<?> serviceInterface, MicroServiceConfig microServiceConfig, ObjectMapper objectMapper) {
    this.serviceName = serviceName;
    this.restClient = restClient;
    this.serviceInterface = serviceInterface;
    this.microServiceConfig = microServiceConfig;
    this.objectMapper = objectMapper;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String requestId = MicroServiceContext.getRequestId();
    Object requestPayload = (args != null && args.length > 0) ? args[0] : null;
    MicroServiceRequest<?> request = MicroServiceRequest.createRequest(requestId, requestPayload);

    String requestJson = objectMapper.writeValueAsString(request);

    String uri = this.microServiceConfig.getUrls().get(serviceName);
    if (uri == null) {
      throw new NotDefinitionServiceUrlException(serviceName);
    }
    HttpMethod httpMethod;

    if (method.isAnnotationPresent(MicroServiceMethod.class)) {
      MicroServiceMethod microServiceMethod = method.getAnnotation(MicroServiceMethod.class);
      httpMethod = HttpMethod.valueOf(microServiceMethod.httpMethod().toUpperCase());
      uri += microServiceMethod.path();
    }
    else {
      throw new IllegalMicroServiceMethodException(serviceName);
    }

    ResponseEntity<String> responseEntity = RestClient.create()
        .method(httpMethod)
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(requestJson)
        .retrieve()
        .toEntity(String.class);

    log.info("%s MicroService send Request To : %s".formatted(MICRO_SERVICE_LOG_PREFIX, uri));

    try {
      String responseBody = responseEntity.getBody();
      log.info("%s MicroService Response from %s -> %s".formatted(MICRO_SERVICE_LOG_PREFIX, uri,
          responseBody));
      if (responseBody == null) {
        throw new MicroServiceNotResponseException(uri);
      }

      MicroServiceResponse<?> genericMicroServiceResponse = this.getGenericMicroServiceResponse(
          responseBody);

      if (!genericMicroServiceResponse.success()) {
        throw new MicroServiceRequestFailException(
            genericMicroServiceResponse.errorStack(),
            httpMethod,
            uri,
            (ErrorWrapper) genericMicroServiceResponse.payload(),
            serviceName
        );
      }

      JavaType type = objectMapper.getTypeFactory().constructParametricType(
          MicroServiceResponse.class,
          method.getReturnType()
      );
      MicroServiceResponse<?> microServiceResponse = objectMapper.readValue(responseBody, type);
      return microServiceResponse.payload();
    } catch (NullPointerException e) {
      throw new MicroServiceNotResponseException(uri);
    }
  }

  private MicroServiceResponse<?> getGenericMicroServiceResponse(String responseBody) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(responseBody, MicroServiceResponse.class);
    }catch (JsonProcessingException e) {
      log.error("%s Can NOT deserialize response to MicroServiceResponse".formatted(
          MICRO_SERVICE_LOG_PREFIX));
      throw new IllegalMicroServiceResponseException("Sent body -> %s".formatted(responseBody), 500);
    }
  }

  private RequestHeadersSpec<?> buildRequestBodySpec(HttpMethod method, String requestJson, String uri) {
    return restClient
        .method(method)
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(requestJson);
  }
}
