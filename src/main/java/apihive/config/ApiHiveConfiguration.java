package apihive.config;

import apihive.config.strategy.DefaultInterfaceDiscoveryStrategy;
import apihive.config.strategy.InterfaceDiscoveryStrategy;
import microservice.config.MicroServiceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(ApiHiveConfig.class)
@ConditionalOnProperty(prefix = "microservice.apihive", name = "enable-api-hive", matchIfMissing = true)
public class ApiHiveConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RestTemplate apiHiveRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  @ConditionalOnMissingBean
  public DtoClassGenerator dtoClassGenerator(ApiHiveConfig apiHiveConfig) {
    return new DtoClassGenerator(apiHiveConfig);
  }

  @Bean
  @ConditionalOnMissingBean(InterfaceDiscoveryStrategy.class)
  public InterfaceDiscoveryStrategy interfaceDiscoveryStrategy(
      MicroServiceConfig microServiceConfig,
      DtoClassGenerator dtoClassGenerator,
      RestTemplate apiHiveRestTemplate,
      ApiHiveConfig config) {
    return new DefaultInterfaceDiscoveryStrategy(
        microServiceConfig,
        dtoClassGenerator,
        apiHiveRestTemplate,
        config
    );
  }
} 