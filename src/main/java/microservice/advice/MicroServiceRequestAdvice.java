package microservice.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import microservice.constants.ConstantStrings;
import microservice.context.MicroServiceContext;
import microservice.exception.NoClientIDException;
import microservice.exception.NoRequestIDException;
import microservice.templates.MicroServiceRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice
public class MicroServiceRequestAdvice extends RequestBodyAdviceAdapter {

  private static final Log log = LogFactory.getLog(MicroServiceRequestAdvice.class);

  @Override
  public boolean supports(MethodParameter methodParameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return MicroServiceRequest.class.isAssignableFrom(methodParameter.getParameterType());
  }

  @Override
  public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
    String body = new BufferedReader(new InputStreamReader(inputMessage.getBody()))
        .lines().collect(Collectors.joining("\n"));

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(body);

    if (!jsonNode.has("requestId") && !jsonNode.has("payload")) {
      ObjectNode wrappedNode = objectMapper.createObjectNode();
      wrappedNode.put("requestId", UUID.randomUUID().toString());
      wrappedNode.put("clientId", ConstantStrings.API_CLIENT_ID);
      wrappedNode.put("timestamp", LocalDateTime.now().toString());
      wrappedNode.set("payload", jsonNode);
      wrappedNode.putPOJO("metadata", null);

      String wrappedJson = objectMapper.writeValueAsString(wrappedNode);

      InputStream newInputStream = new ByteArrayInputStream(wrappedJson.getBytes(StandardCharsets.UTF_8));

      return new HttpInputMessage() {
        @Override
        public InputStream getBody() throws IOException {
          return newInputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
          return inputMessage.getHeaders();
        }
      };
    }

    // 기본 동작 유지
    return inputMessage;
  }


  @Override
  public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    MicroServiceRequest<?> microServiceRequest = (MicroServiceRequest<?>) body;

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
      MicroServiceContext.setClientId(clientId);
    }

    return microServiceRequest;
  }

  @Override
  public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }
}
