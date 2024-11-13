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
import org.springframework.context.annotation.Import;
import microservice.exception.MicroServiceExceptionHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ MicroServiceConfig.class, MicroServiceObjectMapperConfig.class, MicroServiceWebConfig.class, // Configuration
     MicroServiceRequestAdvice.class, MicroServiceResponseAdvice.class, MicroServiceExceptionHandler.class, // Advice
    MicroServiceInterceptor.class, MicroServiceRegistrar.class, // Beans
})
public @interface EnableMicroServices {
  String[] basePackages() default {};
}
