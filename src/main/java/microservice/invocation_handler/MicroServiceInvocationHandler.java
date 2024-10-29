package microservice.invocation_handler;

import static microservice.constants.ConstantStrings.MICRO_SERVICE_LOG_PREFIX;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import microservice.annotations.DeleteRequest;
import microservice.annotations.GetRequest;
import microservice.annotations.PatchRequest;
import microservice.annotations.PostRequest;
import microservice.annotations.PutRequest;
import microservice.config.MicroServiceConfig;
import microservice.context.MicroServiceContext;
import microservice.exception.MicroServiceNotResponseException;
import microservice.templates.MicroServiceRequest;
import microservice.templates.MicroServiceResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

public class MicroServiceInvocationHandler implements InvocationHandler {

  private static final Log log = LogFactory.getLog(MicroServiceInvocationHandler.class);
  private final String baseUrl;
  private final RestClient restClient;
  private final Class<?> serviceInterface;
  private final MicroServiceConfig microServiceConfig;
  private final ObjectMapper objectMapper;

  public MicroServiceInvocationHandler(String baseUrl, RestClient restClient,
      Class<?> serviceInterface, MicroServiceConfig microServiceConfig, ObjectMapper objectMapper) {
    this.baseUrl = baseUrl;
    this.restClient = restClient;
    this.serviceInterface = serviceInterface;
    this.microServiceConfig = microServiceConfig;
    this.objectMapper = objectMapper;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String requestId = MicroServiceContext.getRequestId();
    Object requestPayload = (args != null && args.length > 0) ? args[0] : null;
    MicroServiceRequest<?> request = MicroServiceRequest.createRequest(requestId, requestPayload, microServiceConfig);

    String requestJson = objectMapper.writeValueAsString(request);

    ResponseEntity<String> responseEntity = this.doRequest(method, requestJson);

    try {
      String responseBody = responseEntity.getBody();
      if (responseBody == null) {
        throw new MicroServiceNotResponseException(baseUrl);
      }
      JavaType type = objectMapper.getTypeFactory().constructParametricType(
          MicroServiceResponse.class,
          method.getReturnType()
      );
      MicroServiceResponse<?> microServiceResponse = objectMapper.readValue(responseBody, type);
      return microServiceResponse.payload();
    } catch (NullPointerException e) {
      throw new MicroServiceNotResponseException(baseUrl);
    }
  }

  private ResponseEntity<String> doRequest(Method method, String requestJson) {
    String uri = baseUrl;
    RequestHeadersSpec<?> requestHeadersSpec;

    if (method.isAnnotationPresent(GetRequest.class)) {
      uri += method.getAnnotation(GetRequest.class).value();
      requestHeadersSpec = this.buildRequestBodySpec(HttpMethod.GET, requestJson, uri);

    } else if (method.isAnnotationPresent(PostRequest.class)) {
      uri += method.getAnnotation(PostRequest.class).value();
      requestHeadersSpec = this.buildRequestBodySpec(HttpMethod.POST, requestJson, uri);

    } else if (method.isAnnotationPresent(PatchRequest.class)) {
      uri += method.getAnnotation(PatchRequest.class).value();
      requestHeadersSpec = this.buildRequestBodySpec(HttpMethod.PATCH, requestJson, uri);

    } else if (method.isAnnotationPresent(DeleteRequest.class)) {
      uri += method.getAnnotation(DeleteRequest.class).value();
      requestHeadersSpec = this.buildRequestBodySpec(HttpMethod.DELETE, requestJson, uri);

    } else if (method.isAnnotationPresent(PutRequest.class)) {
      uri += method.getAnnotation(PutRequest.class).value();
      requestHeadersSpec = this.buildRequestBodySpec(HttpMethod.PUT, requestJson, uri);

    }
    else {
      throw new UnsupportedOperationException("Not Supported HTTP Method.");
    }

    ResponseEntity<String> responseEntity = requestHeadersSpec.retrieve().toEntity(String.class);
    log.info("%s MicroService send Request To : %s".formatted(MICRO_SERVICE_LOG_PREFIX, uri));

    return responseEntity;
  }

  private RequestHeadersSpec<?> buildRequestBodySpec(HttpMethod method, String requestJson, String uri) {
    return restClient
        .method(method)
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(requestJson);
  }
}
