package apihive.exposer;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public record ExposeResponse(
    List<ControllerMetadata> controllers
) {

  public static ExposeResponse init() {
    return new ExposeResponse(new ArrayList<>());
  }

  public void addControllerMetadata(RequestMappingInfo requestMappingInfo,
      HandlerMethod handlerMethod) {
    String pattern = String.join(", ", 
        requestMappingInfo.getPatternsCondition().getPatterns());
    String method = String.join(", ",
        requestMappingInfo.getMethodsCondition().getMethods().toString());
        
    Map<String, TypeMetadata> requestFields = new HashMap<>();
    Map<String, TypeMetadata> responseFields = new HashMap<>();
    
    // 파라미터 정보 수집
    Parameter[] parameters = handlerMethod.getMethod().getParameters();
    for (Parameter parameter : parameters) {
      requestFields.put(parameter.getName(), 
          extractTypeMetadata(parameter.getParameterizedType()));
    }
    
    // 리턴 타입 정보 수집
    Type returnType = handlerMethod.getMethod().getGenericReturnType();
    responseFields.put("returnType", extractTypeMetadata(returnType));

    ControllerMetadata metadata = new ControllerMetadata(
        pattern,
        method, 
        handlerMethod.getMethod().getName(),
        requestFields,
        responseFields
    );

    this.controllers.add(metadata);
  }

  private TypeMetadata extractTypeMetadata(Type type) {
    try {
      if (type instanceof ParameterizedType parameterizedType) {
        // List<String>, Map<K,V> 등의 제네릭 타입 처리
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        Type rawType = parameterizedType.getRawType();
        
        List<TypeMetadata> genericTypes = new ArrayList<>();
        for (Type typeArg : typeArguments) {
          genericTypes.add(extractTypeMetadata(typeArg));
        }
        
        return new TypeMetadata(
            rawType.getTypeName(),
            genericTypes,
            new HashMap<>() // 제네릭 타입의 경우 필드 정보는 불필요
        );
      }

      Class<?> clazz = type instanceof Class ? (Class<?>) type : Class.forName(type.getTypeName());
      
      // primitive 타입이나 String 등 기본 타입인 경우
      if (clazz.isPrimitive() || clazz.getName().startsWith("java.lang")) {
        return new TypeMetadata(
            clazz.getName(),
            new ArrayList<>(),
            new HashMap<>()
        );
      }

      // DTO 클래스의 경우 필드 정보 수집
      Map<String, TypeMetadata> fields = new HashMap<>();
      for (Field field : clazz.getDeclaredFields()) {
        fields.put(field.getName(), extractTypeMetadata(field.getGenericType()));
      }

      return new TypeMetadata(
          clazz.getName(),
          new ArrayList<>(),
          fields
      );
      
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to extract type metadata", e);
    }
  }

  public record ControllerMetadata(
    String pattern,
    String method,
    String handlerMethod,
    Map<String, TypeMetadata> requestFields,
    Map<String, TypeMetadata> responseFields
  ) {}

  public record TypeMetadata(
    String typeName,
    List<TypeMetadata> genericTypes,
    Map<String, TypeMetadata> fields
  ) {}
}
