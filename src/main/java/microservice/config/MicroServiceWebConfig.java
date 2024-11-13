package microservice.config;

import microservice.interceptor.MicroServiceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MicroServiceWebConfig implements WebMvcConfigurer {

  private final MicroServiceInterceptor microServiceInterceptor;

  @Autowired
  public MicroServiceWebConfig(MicroServiceInterceptor microServiceInterceptor) {
    this.microServiceInterceptor = microServiceInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(microServiceInterceptor);
  }
}