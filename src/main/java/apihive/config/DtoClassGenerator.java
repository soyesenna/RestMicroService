package apihive.config;

import apihive.exposer.ExposeResponse.TypeMetadata;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class DtoClassGenerator {

  private static final Log log = LogFactory.getLog(DtoClassGenerator.class);
  private final ByteBuddy byteBuddy;
  private final Map<String, Class<?>> generatedClasses;

  public DtoClassGenerator() {
    this.byteBuddy = new ByteBuddy();
    this.generatedClasses = new ConcurrentHashMap<>();
  }

  public Class<?> generateOrGetClass(TypeMetadata metadata) {
    // 이미 생성된 클래스가 있다면 반환
    if (generatedClasses.containsKey(metadata.typeName())) {
      return generatedClasses.get(metadata.typeName());
    }

    try {
      // 기본 타입이나 JDK 클래스인 경우 해당 클래스 반환
      if (isBasicType(metadata.typeName())) {
        return Class.forName(metadata.typeName());
      }

      // DTO 클래스 생성
      DynamicType.Builder<?> classBuilder = byteBuddy
          .subclass(Object.class)
          .name(metadata.typeName());

      // 필드 추가
      for (Map.Entry<String, TypeMetadata> field : metadata.fields().entrySet()) {
        Class<?> fieldType = generateOrGetClass(field.getValue());
        classBuilder = classBuilder
            .defineProperty(field.getKey(), fieldType)
            .annotateField(generateFieldAnnotations(field.getValue()));
      }

      try {
        // 디렉토리 생성
        File generatedDir = new File(DiscoverStrategy.GENERATED_SOURCES_PATH);
        if (!generatedDir.exists()) {
          generatedDir.mkdirs();
        }

        // 패키지 디렉토리 생성
        String packagePath = metadata.typeName().substring(0, metadata.typeName().lastIndexOf('.'));
        File packageDir = new File(generatedDir, packagePath.replace('.', '/'));
        if (!packageDir.exists()) {
          packageDir.mkdirs();
        }

        // 클래스 생성 및 파일로 저장
        DynamicType.Unloaded<?> unloadedType = classBuilder.make();
        unloadedType.saveIn(generatedDir);

        // 클래스 로드
        Class<?> generatedClass = Thread.currentThread().getContextClassLoader()
            .loadClass(metadata.typeName());

        generatedClasses.put(metadata.typeName(), generatedClass);
        return generatedClass;

      } catch (IOException | ClassNotFoundException ex) {
        log.error("Failed to save or load class: " + metadata.typeName(), ex);

        // 파일 저장에 실패한 경우 메모리에만 로드
        Class<?> generatedClass = classBuilder.make()
            .load(Thread.currentThread().getContextClassLoader(),
                ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();

        generatedClasses.put(metadata.typeName(), generatedClass);
        return generatedClass;
      }

    } catch (Exception e) {
      log.error("Failed to generate DTO class: " + metadata.typeName(), e);
      throw new RuntimeException("Failed to generate DTO class", e);
    }
  }

  private boolean isBasicType(String typeName) {
    return typeName.startsWith("java.") ||
        typeName.equals("boolean") ||
        typeName.equals("byte") ||
        typeName.equals("char") ||
        typeName.equals("short") ||
        typeName.equals("int") ||
        typeName.equals("long") ||
        typeName.equals("float") ||
        typeName.equals("double");
  }

  private AnnotationDescription[] generateFieldAnnotations(TypeMetadata fieldMetadata) {
    // TODO: 필요한 경우 필드에 대한 어노테이션 생성
    return new AnnotationDescription[0];
  }
} 