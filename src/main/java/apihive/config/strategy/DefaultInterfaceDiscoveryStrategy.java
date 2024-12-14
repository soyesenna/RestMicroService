package apihive.config.strategy;

import apihive.config.ApiHiveConfig;
import apihive.config.DtoClassGenerator;
import apihive.exposer.ExposeResponse;
import apihive.exposer.ExposeResponse.ControllerMetadata;
import apihive.exposer.ExposeResponse.TypeMetadata;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import microservice.annotations.MicroService;
import microservice.annotations.MicroServiceMethod;
import microservice.config.MicroServiceConfig;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DefaultInterfaceDiscoveryStrategy implements InterfaceDiscoveryStrategy {

  private static final Log log = LogFactory.getLog(DefaultInterfaceDiscoveryStrategy.class);

  private final MicroServiceConfig microServiceConfig;
  private final DtoClassGenerator dtoClassGenerator;
  private final RestTemplate restTemplate;
  private final ApiHiveConfig config;
  private final ByteBuddy byteBuddy;

  public DefaultInterfaceDiscoveryStrategy(
      MicroServiceConfig microServiceConfig,
      DtoClassGenerator dtoClassGenerator,
      RestTemplate apiHiveRestTemplate,
      ApiHiveConfig config) {
    this.microServiceConfig = microServiceConfig;
    this.dtoClassGenerator = dtoClassGenerator;
    this.restTemplate = apiHiveRestTemplate;
    this.config = config;
    this.byteBuddy = new ByteBuddy();
  }

  @Override
  public List<Class<?>> discoverAndCreateInterfaces() {
    List<Class<?>> generatedInterfaces = new ArrayList<>();

    if (!config.getEnableApiHive()) {
      log.info("ApiHive is disabled");
      return generatedInterfaces;
    }

    microServiceConfig.getUrls().forEach((serviceName, baseUrl) -> {
      try {
        Class<?> interfaceClass = createInterfaceForService(serviceName, baseUrl);
        generatedInterfaces.add(interfaceClass);
      } catch (Exception e) {
        log.error("Failed to discover service: " + serviceName, e);
      }
    });

    return generatedInterfaces;
  }

  @Override
  public Class<?> createInterfaceForService(String serviceName, String serviceUrl) {
    try {
      String exposeUrl = serviceUrl + "/api/microservice/metadata/expose";
      ExposeResponse response = restTemplate.getForObject(exposeUrl, ExposeResponse.class);

      if (response != null) {
        return createInterfaceFromMetadata(serviceName, response);
      }
      throw new RuntimeException("Failed to get metadata from service: " + serviceName);
    } catch (Exception e) {
      log.error("Failed to create interface for service: " + serviceName, e);
      throw new RuntimeException("Failed to create interface for service: " + serviceName, e);
    }
  }

  @Override
  public Class<?> createInterfaceFromMetadata(String serviceName, ExposeResponse response) {
    String interfaceName = "apihive.generated." + serviceName + "Client";

    try {
      if (!config.getAlwaysRefresh()) {
        // 이미 로드된 인터페이스가 있는지 확인
        return Class.forName(interfaceName);
      }
    } catch (ClassNotFoundException ignored) {
      // 클래스가 없으면 새로 생성
    }

    DynamicType.Builder<?> interfaceBuilder = byteBuddy
        .makeInterface()
        .name(interfaceName)
        .annotateType(AnnotationDescription.Builder.ofType(MicroService.class)
            .define("value", serviceName)
            .build());

    // 각 컨트롤러 메서드에 대한 인터페이스 메서드 생성
    for (ControllerMetadata metadata : response.controllers()) {
      interfaceBuilder = addInterfaceMethod(interfaceBuilder, metadata);
    }

    try {
      // 디렉토리 생성
      File generatedDir = new File(config.getGeneratedSourcesPath());
      if (!generatedDir.exists()) {
        generatedDir.mkdirs();
      }

      // 패키지 디렉토리 생성
      String packagePath = interfaceName.substring(0, interfaceName.lastIndexOf('.'));
      File packageDir = new File(generatedDir, packagePath.replace('.', '/'));
      if (!packageDir.exists()) {
        packageDir.mkdirs();
      }

      // 생성된 바이트코드를 파일로 저장
      DynamicType.Unloaded<?> unloadedType = interfaceBuilder.make();
      unloadedType.saveIn(generatedDir);

      // 클래스로더 전략에 따라 로드
      ClassLoader classLoader;
      switch (config.getClassLoaderStrategy()) {
        case SYSTEM:
          classLoader = ClassLoader.getSystemClassLoader();
          break;
        case CUSTOM:
          classLoader = getClass().getClassLoader();
          break;
        case THREAD_CONTEXT:
        default:
          classLoader = Thread.currentThread().getContextClassLoader();
      }

      return classLoader.loadClass(interfaceName);

    } catch (IOException | ClassNotFoundException ex) {
      log.error("Failed to save or load interface: " + interfaceName, ex);

      // 파일 저장에 실패한 경우 메모리에만 로드
      return interfaceBuilder.make()
          .load(Thread.currentThread().getContextClassLoader(),
              ClassLoadingStrategy.Default.INJECTION)
          .getLoaded();
    }
  }

  private DynamicType.Builder<?> addInterfaceMethod(
      DynamicType.Builder<?> interfaceBuilder,
      ControllerMetadata metadata) {

    // 메서드 파라미터 타입 생성
    List<TypeDescription.Generic> parameterTypes = metadata.requestFields().values().stream()
        .map(this::createTypeFromMetadata)
        .collect(Collectors.toList());

    // 리턴 타입 생성
    TypeDescription.Generic returnType = createTypeFromMetadata(
        metadata.responseFields().get("returnType"));

    return interfaceBuilder
        .defineMethod(metadata.handlerMethod(), returnType)
        .withParameters(parameterTypes)
        .withoutCode()
        .annotateMethod(AnnotationDescription.Builder.ofType(MicroServiceMethod.class)
            .define("httpMethod", metadata.method())
            .define("path", metadata.pattern())
            .build());
  }

  private TypeDescription.Generic createTypeFromMetadata(TypeMetadata metadata) {
    try {
      if (metadata.genericTypes().isEmpty()) {
        // 제네릭이 없는 일반 타입
        Class<?> type = dtoClassGenerator.generateOrGetClass(metadata);
        return TypeDescription.ForLoadedType.of(type).asGenericType();
      } else {
        // 제네릭 타입 (List<T>, Map<K,V> 등)
        Class<?> rawType = Class.forName(metadata.typeName());
        List<TypeDescription.Generic> typeArguments = metadata.genericTypes().stream()
            .map(this::createTypeFromMetadata)
            .collect(Collectors.toList());

        return TypeDescription.Generic.Builder.parameterizedType(
            TypeDescription.ForLoadedType.of(rawType),
            typeArguments
        ).build();
      }
    } catch (ClassNotFoundException e) {
      log.error("Failed to create type from metadata: " + metadata.typeName(), e);
      return TypeDescription.Generic.OBJECT;
    }
  }
} 