package apihive.register;

import apihive.config.ApiHiveConfig;
import apihive.config.ApiHiveConfiguration;
import apihive.config.InterfaceLoaderFactoryBean;
import apihive.config.strategy.InterfaceDiscoveryStrategy;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiHiveBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {
    // ApiHive 설정 bean 등록
    registerApiHiveConfigBean(registry);

    // ApiHive 설정을 로드하는 Configuration 등록
    registerApiHiveConfiguration(registry);

    // InterfaceLoader bean 등록 - 실제 인터페이스 생성은 여기서 수행
    registerInterfaceLoaderBean(registry);
  }

  private void registerApiHiveConfigBean(BeanDefinitionRegistry registry) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder
        .genericBeanDefinition(ApiHiveConfig.class)
        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

    registry.registerBeanDefinition("apiHiveConfig", builder.getBeanDefinition());
  }

  private void registerApiHiveConfiguration(BeanDefinitionRegistry registry) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder
        .genericBeanDefinition(ApiHiveConfiguration.class)
        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

    registry.registerBeanDefinition(
        "apiHiveConfiguration",
        builder.getBeanDefinition()
    );
  }

  private void registerInterfaceLoaderBean(BeanDefinitionRegistry registry) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder
        .genericBeanDefinition(InterfaceLoaderFactoryBean.class)
        .addConstructorArgReference("apiHiveConfig")
        .addConstructorArgReference("interfaceDiscoveryStrategy")
        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
        .setInitMethodName("loadInterfaces");

    registry.registerBeanDefinition(
        "interfaceLoader",
        builder.getBeanDefinition()
    );
  }
} 