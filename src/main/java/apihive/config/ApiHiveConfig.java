package apihive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "microservice.apihive")
@Component("microServiceApiHiveConfig")
public class ApiHiveConfig {

  Boolean enableApiHive = false;
  Boolean enableControllerExposer = false;
  Boolean alwaysRefresh = false;

  public Boolean getEnableApiHive() {
    return enableApiHive;
  }

  public void setEnableApiHive(Boolean enableApiHive) {
    this.enableApiHive = enableApiHive;
  }

  public Boolean getEnableControllerExposer() {
    return enableControllerExposer;
  }

  public void setEnableControllerExposer(Boolean enableControllerExposer) {
    this.enableControllerExposer = enableControllerExposer;
  }

  public Boolean getAlwaysRefresh() {
    return alwaysRefresh;
  }

  public void setAlwaysRefresh(Boolean alwaysRefresh) {
    this.alwaysRefresh = alwaysRefresh;
  }
}
