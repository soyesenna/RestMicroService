package microservice.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import microservice.annotations.MicroServiceController;
import microservice.annotations.MicroServiceIgnore;
import microservice.constants.Constants;
import microservice.templates.MicroServiceRequest;
import microservice.utils.MicroServiceContextInitUtil;
import microservice.utils.MicroServicePayloadValidationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice(annotations = MicroServiceController.class)
public class MicroServiceRequestAdvice extends RequestBodyAdviceAdapter {

  private static final Log log = LogFactory.getLog(MicroServiceRequestAdvice.class);

  private final MicroServicePayloadValidationUtil microServicePayloadValidationUtil;
  private final MicroServiceContextInitUtil microServiceContextInitUtil;

  public MicroServiceRequestAdvice(MicroServicePayloadValidationUtil microServicePayloadValidationUtil, MicroServiceContextInitUtil microServiceContextInitUtil) {
    this.microServicePayloadValidationUtil = microServicePayloadValidationUtil;
    this.microServiceContextInitUtil = microServiceContextInitUtil;
  }

  @Override
  public boolean supports(MethodParameter methodParameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    Annotation[] methodAnnotations = methodParameter.getMethodAnnotations();
    for (Annotation methodAnnotation : methodAnnotations) {
      if (methodAnnotation.annotationType().equals(MicroServiceIgnore.class)) {
        log.info("%s MicroServiceIgnore annotation found, skipping MicroServiceRequestAdvice".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));
        return false;
      }
    }

    log.info("%s MicroServiceRequestAdvice supports to type -> [%s]".formatted(Constants.MICRO_SERVICE_LOG_PREFIX, targetType.getTypeName()));
    return true;
  }

  @Override
  public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
      Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    MicroServiceRequest<?> microServiceRequest = objectMapper.readValue(inputMessage.getBody(),
        MicroServiceRequest.class);

    this.microServiceContextInitUtil.init(microServiceRequest);

    return new HttpInputMessage() {
      @Override
      public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(microServiceRequest.payload()));
      }

      @Override
      public HttpHeaders getHeaders() {
        return inputMessage.getHeaders();
      }
    };
  }

  @Override
  public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
      Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
    this.microServicePayloadValidationUtil.validatePayload(body);
    return body;
  }

  @Override
  public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
      MethodParameter parameter, Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }
}
