package apihive.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import apihive.exposer.ExposeResponse;
import apihive.exposer.ExposeResponse.ControllerMetadata;
import apihive.exposer.ExposeResponse.TypeMetadata;
import microservice.annotations.MicroService;
import microservice.annotations.MicroServiceMethod;
import microservice.config.MicroServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DiscoverStrategyTest {
    @Mock
    private MicroServiceConfig microServiceConfig;
    
    @Mock
    private RestTemplate restTemplate;
    
    private DtoClassGenerator dtoClassGenerator;
    private DiscoverStrategy discoverStrategy;

    @BeforeEach
    void setUp() {
        dtoClassGenerator = new DtoClassGenerator();
        discoverStrategy = new DiscoverStrategy(microServiceConfig, dtoClassGenerator) {
            @Override
            protected RestTemplate getRestTemplate() {
                return restTemplate;
            }
        };
        
        Map<String, String> urls = new HashMap<>();
        urls.put("testService", "http://localhost:8080");
        when(microServiceConfig.getUrls()).thenReturn(urls);
    }

    @Test
    void shouldGenerateInterfaceFromExposeResponse() {
        // Given
        ExposeResponse exposeResponse = createSampleExposeResponse();
        when(restTemplate.getForObject(anyString(), eq(ExposeResponse.class)))
            .thenReturn(exposeResponse);

        // When
        List<Class<?>> interfaces = discoverStrategy.discoverAndCreateInterfaces();

        // Then
        assertFalse(interfaces.isEmpty());
        Class<?> generatedInterface = interfaces.get(0);
        
        // 인터페이스 검증
        assertTrue(generatedInterface.isInterface());
        assertTrue(generatedInterface.isAnnotationPresent(MicroService.class));
        assertEquals("testService", 
            generatedInterface.getAnnotation(MicroService.class).value());

        // 메서드 검증
        Method[] methods = generatedInterface.getDeclaredMethods();
        assertEquals(1, methods.length);
        
        Method method = methods[0];
        assertTrue(method.isAnnotationPresent(MicroServiceMethod.class));
        assertEquals("getUserInfo", method.getName());
    }

    private ExposeResponse createSampleExposeResponse() {
        // UserDTO 타입 메타데이터 생성
        Map<String, TypeMetadata> userDtoFields = new HashMap<>();
        userDtoFields.put("name", new TypeMetadata(
            "java.lang.String",
            new ArrayList<>(),
            new HashMap<>()
        ));
        userDtoFields.put("age", new TypeMetadata(
            "java.lang.Integer",
            new ArrayList<>(),
            new HashMap<>()
        ));

        TypeMetadata userDtoType = new TypeMetadata(
            "com.example.UserDTO",
            new ArrayList<>(),
            userDtoFields
        );

        // 컨트롤러 메타데이터 생성
        Map<String, TypeMetadata> requestFields = new HashMap<>();
        requestFields.put("id", new TypeMetadata(
            "java.lang.Long",
            new ArrayList<>(),
            new HashMap<>()
        ));

        Map<String, TypeMetadata> responseFields = new HashMap<>();
        responseFields.put("returnType", userDtoType);

        ControllerMetadata controllerMetadata = new ControllerMetadata(
            "/api/users/{id}",
            "GET",
            "getUserInfo",
            requestFields,
            responseFields
        );

        return new ExposeResponse(List.of(controllerMetadata));
    }
} 