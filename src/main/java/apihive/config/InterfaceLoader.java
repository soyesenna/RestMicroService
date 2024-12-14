package apihive.config;

import apihive.config.strategy.InterfaceDiscoveryStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

@Configuration
@Import(InterfaceDiscoveryStrategy.class)
@DependsOn("microServiceConfig")
public class InterfaceLoader {

  private static final String GENERATED_SOURCES_PATH = "build/generated/sources/apihive";
  private final InterfaceDiscoveryStrategy discoverStrategy;

  public InterfaceLoader(InterfaceDiscoveryStrategy discoverStrategy) {
    this.discoverStrategy = discoverStrategy;
  }

  @PostConstruct
  public void loadInterfaces() {
    // 생성된 소스 디렉토리 추가
    addGeneratedSourcesToClasspath();

    List<Class<?>> interfaces = discoverStrategy.discoverAndCreateInterfaces();
    // 생성된 인터페이스들은 MicroServiceRegistrar에 의해 자동으로 빈으로 등록됩니다
  }

  private void addGeneratedSourcesToClasspath() {
    try {
      File generatedDir = new File(GENERATED_SOURCES_PATH);
      if (!generatedDir.exists()) {
        generatedDir.mkdirs();
      }

      // 생성된 소스 디렉토리를 클래스패스에 추가
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(Thread.currentThread().getContextClassLoader(),
          generatedDir.toURI().toURL());

    } catch (Exception e) {
      throw new RuntimeException("Failed to add generated sources to classpath", e);
    }
  }
}
