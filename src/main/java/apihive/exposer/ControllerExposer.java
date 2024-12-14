package apihive.exposer;

import microservice.config.MicroServiceConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/microservice/metadata")
public class ControllerExposer {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;
  private final MicroServiceConfig microServiceConfig;

  public ControllerExposer(RequestMappingHandlerMapping requestMappingHandlerMapping,
      MicroServiceConfig microServiceConfig) {
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.microServiceConfig = microServiceConfig;
  }

  @GetMapping("/expose")
  public ResponseEntity<ExposeResponse> expose() {
    if (!this.microServiceConfig.getEnableControllerExposer()) {
      return ResponseEntity.status(404).body(null);
    }

    ExposeResponse exposeResponse = ExposeResponse.init();

    requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
      assert requestMappingInfo.getPatternsCondition() != null;
      String urls = String.join(", ", requestMappingInfo.getPatternsCondition().getPatterns());
      String methods = String.join(", ",
          requestMappingInfo.getMethodsCondition().getMethods().toString());
      String handlerMethodName = handlerMethod.getMethod().getName();

      // 메서드의 파라미터와 리턴 타입 정보 수집
      Map<String, String> requestFields = new HashMap<>();
      Map<String, String> responseFields = new HashMap<>();
      
      // 파라미터 정보 수집
      Parameter[] parameters = handlerMethod.getMethod().getParameters();
      for (Parameter parameter : parameters) {
        requestFields.put(parameter.getName(), parameter.getType().getSimpleName());
      }
      
      // 리턴 타입 정보 수집
      Type returnType = handlerMethod.getMethod().getGenericReturnType();
      responseFields.put("returnType", returnType.getTypeName());

      exposeResponse.addControllerMetadata(requestMappingInfo, handlerMethod);
    });

    return ResponseEntity.ok(exposeResponse);
  }
}
