package microservice.register;

import java.util.Map;
import java.util.Set;
import microservice.annotations.EnableMicroServices;
import microservice.annotations.MicroService;
import microservice.constants.Constants;
import microservice.exception.MicroServiceCreateException;
import microservice.factorybean.MicroServiceFactoryBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class MicroServiceRegistrar implements ImportBeanDefinitionRegistrar {

  private static final Log log = LogFactory.getLog(MicroServiceRegistrar.class);

  @Override
  public void registerBeanDefinitions(
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

    Map<String, Object> attributes = importingClassMetadata
        .getAnnotationAttributes(EnableMicroServices.class.getName());
    assert attributes != null;
    String[] basePackages = (String[]) attributes.get("basePackages");

   log.info("%s MicroService interfaces Scanning...".formatted(Constants.MICRO_SERVICE_LOG_PREFIX));

    if (basePackages == null || basePackages.length == 0) {
      basePackages = new String[] {
          ((StandardAnnotationMetadata) importingClassMetadata)
              .getIntrospectedClass().getPackage().getName()
      };
    }

    InterfaceScanningCandidateComponentProvider scanner =
        new InterfaceScanningCandidateComponentProvider();
    scanner.addIncludeFilter(new AnnotationTypeFilter(MicroService.class));

    for (String basePackage : basePackages) {
      Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
      log.info(Constants.MICRO_SERVICE_LOG_PREFIX + " Found " + candidateComponents.size() + " components in " + basePackage);

      for (BeanDefinition bd : candidateComponents) {
        String className = bd.getBeanClassName();

        try {
          Class<?> clazz = Class.forName(className);
          if (clazz.isInterface()) {
            BeanDefinitionBuilder beanDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(MicroServiceFactoryBean.class);
            beanDefinitionBuilder.addPropertyValue("serviceInterface", clazz);
            beanDefinitionBuilder.addConstructorArgReference("microServiceConfig");
            beanDefinitionBuilder.addConstructorArgReference("microServiceObjectMapper");
            beanDefinitionBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinitionBuilder.setPrimary(true);
            beanDefinitionBuilder.setLazyInit(true);
            beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
            beanDefinitionBuilder.addDependsOn("microServiceConfig");
            beanDefinitionBuilder.addDependsOn("microServiceObjectMapper");

            log.info("%s MicroService Bean created : ".formatted(Constants.MICRO_SERVICE_LOG_PREFIX) + className);
          }
        } catch (ClassNotFoundException e) {
          log.error("%s Can NOT Create MicroService Bean. : ".formatted(Constants.MICRO_SERVICE_LOG_PREFIX) + e.getMessage());
          throw new MicroServiceCreateException("Can NOT Create MicroService Bean. : " + e.getMessage());
        }
      }
    }
  }
}
