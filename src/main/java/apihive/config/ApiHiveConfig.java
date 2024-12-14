package apihive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "microservice.apihive")
@Component("apiHiveConfig")
public class ApiHiveConfig {

  Boolean enableApiHive = false;
  Boolean enableControllerExposer = false;
  Boolean alwaysRefresh = false;
  String generatedSourcesPath = "build/generated/sources/apihive";
  ClassLoaderStrategy classLoaderStrategy = ClassLoaderStrategy.THREAD_CONTEXT;

  public enum ClassLoaderStrategy {
    THREAD_CONTEXT,
    SYSTEM,
    CUSTOM
  }

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

  public String getGeneratedSourcesPath() {
    return generatedSourcesPath;
  }

  public void setGeneratedSourcesPath(String generatedSourcesPath) {
    this.generatedSourcesPath = generatedSourcesPath;
  }

  public ClassLoaderStrategy getClassLoaderStrategy() {
    return classLoaderStrategy;
  }

  public void setClassLoaderStrategy(ClassLoaderStrategy classLoaderStrategy) {
    this.classLoaderStrategy = classLoaderStrategy;
  }
}
