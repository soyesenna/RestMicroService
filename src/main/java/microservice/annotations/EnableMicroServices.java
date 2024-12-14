package microservice.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import microservice.advice.MicroServiceRequestAdvice;
import microservice.advice.MicroServiceResponseAdvice;
import microservice.config.MicroServiceConfig;
import microservice.config.MicroServiceObjectMapperConfig;
import microservice.config.MicroServiceWebConfig;
import microservice.interceptor.MicroServiceInterceptor;
import microservice.register.MicroServiceRegistrar;
import microservice.utils.MicroServiceContextInitUtil;
import microservice.utils.MicroServicePayloadValidationUtil;
import org.springframework.context.annotation.Import;
import microservice.exception.MicroServiceExceptionHandler;
import apihive.register.ApiHiveBeanDefinitionRegistrar;
import apihive.config.ApiHiveConfig;
import apihive.config.ApiHiveConfiguration;
import apihive.config.DtoClassGenerator;
import apihive.config.InterfaceLoader;
import apihive.config.InterfaceLoaderFactoryBean;
import apihive.config.strategy.DefaultInterfaceDiscoveryStrategy;
import apihive.config.strategy.InterfaceDiscoveryStrategy;
import apihive.exposer.ControllerExposer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ MicroServiceConfig.class, MicroServiceObjectMapperConfig.class, MicroServiceWebConfig.class, // Configuration
    MicroServiceRequestAdvice.class, MicroServiceResponseAdvice.class, MicroServiceExceptionHandler.class, // Advice
    MicroServiceInterceptor.class, MicroServiceRegistrar.class, MicroServiceContextInitUtil.class,
    MicroServicePayloadValidationUtil.class, // Beans
    ApiHiveConfig.class,
    ApiHiveConfiguration.class,
    DtoClassGenerator.class,
    DefaultInterfaceDiscoveryStrategy.class,
    InterfaceLoader.class,
    InterfaceLoaderFactoryBean.class,
    ControllerExposer.class,
    ApiHiveBeanDefinitionRegistrar.class // ApiHive 등록기 추가
})
public @interface EnableMicroServices {
  String[] basePackages() default {};
}
