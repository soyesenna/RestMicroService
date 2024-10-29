package microservice.register;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class InterfaceScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

  public InterfaceScanningCandidateComponentProvider() {
    super(false);
  }

  @Override
  protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    return true;
  }
}
