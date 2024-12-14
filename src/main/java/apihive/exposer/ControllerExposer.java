package apihive.exposer;

import apihive.config.ApiHiveConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Controller
@RequestMapping("/api/microservice/metadata")
public class ControllerExposer {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;
  private final ApiHiveConfig apiHiveConfig;

  public ControllerExposer(RequestMappingHandlerMapping requestMappingHandlerMapping,
      ApiHiveConfig apiHiveConfig) {
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.apiHiveConfig = apiHiveConfig;
  }

  @GetMapping("/expose")
  public ResponseEntity<ExposeResponse> expose() {
    if (!this.apiHiveConfig.getEnableControllerExposer()) {
      return ResponseEntity.status(404).body(null);
    }

    ExposeResponse exposeResponse = ExposeResponse.init();

    requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
      assert requestMappingInfo.getPatternsCondition() != null;

      exposeResponse.addControllerMetadata(requestMappingInfo, handlerMethod);
    });

    return ResponseEntity.ok(exposeResponse);
  }
}
