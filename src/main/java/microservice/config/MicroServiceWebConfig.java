package microservice.config;

import microservice.interceptors.MicroServiceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MicroServiceWebConfig implements WebMvcConfigurer {

  private final MicroServiceInterceptor microServiceInterceptor;

  public MicroServiceWebConfig(MicroServiceInterceptor microServiceInterceptor) {
    this.microServiceInterceptor = microServiceInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(microServiceInterceptor)
        .addPathPatterns("/**");
  }
}
