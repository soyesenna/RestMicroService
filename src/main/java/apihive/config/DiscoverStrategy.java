package apihive.config;

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
public class DiscoverStrategy {

  private static final Log log = LogFactory.getLog(DiscoverStrategy.class);
  private final RestTemplate restTemplate;
  private final MicroServiceConfig microServiceConfig;
  private final ByteBuddy byteBuddy;
  private final DtoClassGenerator dtoClassGenerator;
  public static final String GENERATED_SOURCES_PATH = "build/generated/sources/apihive";

  public DiscoverStrategy(MicroServiceConfig microServiceConfig,
      DtoClassGenerator dtoClassGenerator) {
    this.restTemplate = new RestTemplate();
    this.microServiceConfig = microServiceConfig;
    this.byteBuddy = new ByteBuddy();
    this.dtoClassGenerator = dtoClassGenerator;
  }

  public List<Class<?>> discoverAndCreateInterfaces() {
    List<Class<?>> generatedInterfaces = new ArrayList<>();

    // microservice.urls에 설정된 모든 서비스를 순회
    microServiceConfig.getUrls().forEach((serviceName, baseUrl) -> {
      try {
        String exposeUrl = baseUrl + "/api/microservice/metadata/expose";
        ExposeResponse response = restTemplate.getForObject(exposeUrl, ExposeResponse.class);

        if (response != null) {
          Class<?> interfaceClass = generateInterface(serviceName, response);
          generatedInterfaces.add(interfaceClass);
        }
      } catch (Exception e) {
        log.error("Failed to discover service: " + serviceName, e);
      }
    });

    return generatedInterfaces;
  }

  private Class<?> generateInterface(String serviceName, ExposeResponse response) {
    String interfaceName = "apihive.generated." + serviceName + "Client";
    
    try {
      // 이미 로드된 인터페이스가 있는지 확인
      return Class.forName(interfaceName);
    } catch (ClassNotFoundException e) {
      // 새로 생성
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

      // 파일 시스템에 저장 및 로드
      try {
        // 디렉토리 생성
        File generatedDir = new File(GENERATED_SOURCES_PATH);
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

        // 현재 스레드의 컨텍스트 클래스로더를 사용하여 로드
        return Thread.currentThread().getContextClassLoader()
            .loadClass(interfaceName);
        
      } catch (IOException | ClassNotFoundException ex) {
        log.error("Failed to save or load interface: " + interfaceName, ex);
        
        // 파일 저장에 실패한 경우 메모리에만 로드
        return interfaceBuilder.make()
            .load(Thread.currentThread().getContextClassLoader(), 
                  ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
      }
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

  protected RestTemplate getRestTemplate() {
    return this.restTemplate;
  }
}
