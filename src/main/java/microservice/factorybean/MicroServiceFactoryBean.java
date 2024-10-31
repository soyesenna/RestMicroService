package microservice.factorybean;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Proxy;
import microservice.annotations.MicroService;
import microservice.config.MicroServiceConfig;
import microservice.invocation_handler.MicroServiceInvocationHandler;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestClient;

public class MicroServiceFactoryBean<T> implements FactoryBean<T>, InitializingBean {

  private Class<T> serviceInterface;
  private T proxyInstance;
  private final RestClient restClient;
  private final MicroServiceConfig microServiceConfig;
  private final ObjectMapper microServiceObjectMapper;

  public MicroServiceFactoryBean(MicroServiceConfig microServiceConfig, ObjectMapper microServiceObjectMapper) {
    this.microServiceConfig = microServiceConfig;
    this.microServiceObjectMapper = microServiceObjectMapper;
    this.restClient = RestClient.create();
  }

  public void setServiceInterface(Class<T> serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  @Override
  public T getObject() {
    return proxyInstance;
  }

  @Override
  public Class<?> getObjectType() {
    return serviceInterface;
  }

  @Override
  public void afterPropertiesSet() {
    MicroService microService = serviceInterface.getAnnotation(MicroService.class);
    String serviceName = microService.value();

    proxyInstance = (T) Proxy.newProxyInstance(
        serviceInterface.getClassLoader(),
        new Class[] { serviceInterface },
        new MicroServiceInvocationHandler(serviceName, restClient, serviceInterface, microServiceConfig, microServiceObjectMapper)
    );
  }
}
