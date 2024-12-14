package apihive.config;

import static org.junit.jupiter.api.Assertions.*;

import apihive.exposer.ExposeResponse.TypeMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DtoClassGeneratorTest {
    private DtoClassGenerator dtoClassGenerator;

    @BeforeEach
    void setUp() {
        dtoClassGenerator = new DtoClassGenerator();
    }

    @Test
    void shouldGenerateSimpleDto() throws NoSuchMethodException {
        // Given
        Map<String, TypeMetadata> fields = new HashMap<>();
        fields.put("name", new TypeMetadata(
            "java.lang.String",
            new ArrayList<>(),
            new HashMap<>()
        ));
        fields.put("age", new TypeMetadata(
            "java.lang.Integer",
            new ArrayList<>(),
            new HashMap<>()
        ));

        TypeMetadata metadata = new TypeMetadata(
            "com.example.SimpleDTO",
            new ArrayList<>(),
            fields
        );

        // When
        Class<?> generatedClass = dtoClassGenerator.generateOrGetClass(metadata);

        // Then
        assertNotNull(generatedClass);
        assertEquals("com.example.SimpleDTO", generatedClass.getName());
        assertEquals(2, generatedClass.getDeclaredFields().length);
        
        // getter/setter 메서드 검증
        assertNotNull(generatedClass.getDeclaredMethod("getName"));
        assertNotNull(generatedClass.getDeclaredMethod("setName", String.class));
        assertNotNull(generatedClass.getDeclaredMethod("getAge"));
        assertNotNull(generatedClass.getDeclaredMethod("setAge", Integer.class));
    }

    @Test
    void shouldGenerateNestedDto() throws NoSuchFieldException {
        // Given
        // Address DTO 필드
        Map<String, TypeMetadata> addressFields = new HashMap<>();
        addressFields.put("street", new TypeMetadata(
            "java.lang.String",
            new ArrayList<>(),
            new HashMap<>()
        ));

        TypeMetadata addressType = new TypeMetadata(
            "com.example.AddressDTO",
            new ArrayList<>(),
            addressFields
        );

        // User DTO 필드 (Address 포함)
        Map<String, TypeMetadata> userFields = new HashMap<>();
        userFields.put("name", new TypeMetadata(
            "java.lang.String",
            new ArrayList<>(),
            new HashMap<>()
        ));
        userFields.put("address", addressType);

        TypeMetadata userType = new TypeMetadata(
            "com.example.UserDTO",
            new ArrayList<>(),
            userFields
        );

        // When
        Class<?> userClass = dtoClassGenerator.generateOrGetClass(userType);

        // Then
        assertNotNull(userClass);
        assertEquals("com.example.UserDTO", userClass.getName());
        
        // address 필드의 타입이 올바르게 생성되었는지 확인
        Class<?> addressFieldType = userClass.getDeclaredField("address").getType();
        assertEquals("com.example.AddressDTO", addressFieldType.getName());
    }
} 