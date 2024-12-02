package microservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import microservice.annotations.MicroServiceController;
import microservice.constants.Constants;
import microservice.context.MicroServiceContext;
import microservice.templates.MicroServiceRequest;
import microservice.utils.MicroServiceContextInitUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MicroServiceInterceptor implements HandlerInterceptor {

  private static final Log log = LogFactory.getLog(MicroServiceInterceptor.class);

  private final MicroServiceContextInitUtil microServiceContextInitUtil;

  public MicroServiceInterceptor(MicroServiceContextInitUtil microServiceContextInitUtil) {
    this.microServiceContextInitUtil = microServiceContextInitUtil;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (this.isSupport(handler)) {
      log.info("%s MicroServiceController found, initializing MicroServiceContext".formatted(
          Constants.MICRO_SERVICE_LOG_PREFIX));
      ObjectMapper objectMapper = new ObjectMapper();
      String body = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      MicroServiceRequest<?> microServiceRequest = objectMapper.readValue(body,
          MicroServiceRequest.class);
      this.microServiceContextInitUtil.init(microServiceRequest);
    }

    return true;
  }

  private boolean isSupport(Object handler) {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;

      boolean isMicroServiceController = Arrays.stream(handlerMethod.getBeanType().getAnnotations())
          .anyMatch(annotation -> annotation.annotationType().equals(MicroServiceController.class));

      boolean hasRequestBody = Arrays.stream(handlerMethod.getMethodParameters())
          .anyMatch(parameter -> parameter.hasParameterAnnotation(RequestBody.class));

      return isMicroServiceController && !hasRequestBody;
    }
    return false;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    MicroServiceContext.clear();
  }
}
