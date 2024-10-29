# 🌐 MicroService Communication Abstraction for Spring Boot

Welcome to the **MicroService Communication Abstraction** project! This library simplifies inter-service communication in a microservice architecture using REST APIs with Spring Boot. By dynamically generating proxy beans for interfaces annotated with `@MicroService` at runtime and registering them in the Spring DI Container, developers can focus on building business logic without worrying about the intricacies of HTTP communication between microservices.

## 📖 Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [Enable MicroServices](#1-enable-microservices)
  - [Define MicroService Interfaces](#2-define-microservice-interfaces)
  - [Define Methods with Annotations](#3-define-methods-with-annotations)
  - [Handle Requests and Responses](#4-handle-requests-and-responses)
  - [Consume the MicroService](#5-consume-the-microservice)
- [Important Points](#important-points)
- [Exception Handling](#exception-handling)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

- **Simplified Inter-Service Communication**: Abstracts the HTTP communication between microservices.
- **Dynamic Proxy Generation**: Automatically generates proxy implementations for interfaces annotated with `@MicroService`.
- **Request and Response Wrapping**: All requests and responses are encapsulated in `MicroServiceRequest` and `MicroServiceResponse` objects.
- **Validation Support**: Supports payload validation using Jakarta Validation and Hibernate Validator.
- **Unified Exception Handling**: All exceptions inherit from `MicroServiceException` for centralized handling.
- **Transparent Integration**: Seamlessly integrates with Spring Boot applications.

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Spring Boot 3.2** or higher

### Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.soyesenna</groupId>
    <artifactId>rest-microservice</artifactId>
    <version>0.0.1</version>
</dependency>
```

Add the dependency to your `build.gradle`:
```gradle
implementation group: 'io.github.soyesenna', name: 'rest-microservice', version: '0.0.1'
```

or

```gradle
implementation("io.github.soyesenna:rest-microservice:0.0.1")
```

## 🛠 Usage

### 1. Enable MicroServices

Annotate your main application class with `@EnableMicroServices` to activate the library:

```java
@EnableMicroServices(basePackages = "com.example.services")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. Define MicroService Interfaces

Create an interface to represent the remote microservice you want to communicate with. Annotate it with `@MicroService` and specify the base URL:

```java
@MicroService("http://user-service")
public interface UserService {

    // Method definitions...
}
```

### 3. Define Methods with Annotations

Inside your interface, define methods representing the endpoints you wish to call, using the provided annotations:

- `@GetRequest("/endpoint")`
- `@PostRequest("/endpoint")`
- `@PutRequest("/endpoint")`
- `@PatchRequest("/endpoint")`
- `@DeleteRequest("/endpoint")`

Each method's parameter represents the payload to send, and the return type represents the expected response payload.

```java
@GetRequest("/users/{id}")
UserDto getUserById(@PathVariable Long id);

@PostRequest("/users")
void createUser(@RequestBody UserCreateDto userCreateDto);

// Other methods...
```

### 4. Handle Requests and Responses

- **Requests**: All requests are wrapped in a `MicroServiceRequest` object. Your controller methods should accept parameters of type `MicroServiceRequest<?>`.

- **Validation**: Use validation annotations on your DTO classes. If validation fails, a `MicroServiceRequestPayloadValidationFailException` is thrown.

- **Responses**: All responses are wrapped in a `MicroServiceResponse` object. Controller methods should return pure Java objects, not wrapped in `MicroServiceResponse`. The response wrapping is handled automatically.

- **Client ID**: When wrapping responses, the library automatically sets the `clientId` field using the value from your configuration.

### 5. Consume the MicroService

Inject the interface into your services or controllers as you would with any other Spring bean:

```java
@Service
public class MyService {

    private final UserService userService;

    public MyService(UserService userService) {
        this.userService = userService;
    }

    public UserDto getUser(Long id) {
        return userService.getUserById(id);
    }
}
```

## 📌 Important Points

- **Interface Definition**: Define an interface for each microservice you want to communicate with, annotating it with `@MicroService` and specifying the base URL.

- **Method Annotations**: Use the HTTP method annotations on interface methods to specify the endpoint and HTTP method:

    - `@GetRequest("/endpoint")`
    - `@PostRequest("/endpoint")`
    - `@PutRequest("/endpoint")`
    - `@PatchRequest("/endpoint")`
    - `@DeleteRequest("/endpoint")`

- **Parameters and Return Types**:

    - **Parameters**: The method parameters represent the payload to be sent. These are wrapped inside `MicroServiceRequest` and stored in the `payload` field.

    - **Return Types**: The method's return type represents the expected payload from the response. The library extracts the `payload` field from `MicroServiceResponse` and maps it to your specified return type.

- **Context Management**: `MicroServiceContext` is initialized at the end of each request by `MicroServiceInterceptor`, ensuring thread safety and proper context management.

- **Exception Handling**: All exceptions extend `MicroServiceException`, allowing you to handle them globally using Spring's `@ControllerAdvice` or other exception handling mechanisms.

## ⚠️ Exception Handling

The library provides a unified exception hierarchy:

- **`MicroServiceException`**: Base class for all custom exceptions.
- **`MicroServiceRequestPayloadValidationFailException`**: Thrown when payload validation fails.
- **Other Exceptions**: Include `NoRequestIDException`, `NoClientIDException`, `MicroServiceNotResponseException`, etc.

You can implement a global exception handler to catch these exceptions:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MicroServiceException.class)
    public ResponseEntity<ErrorInfo> handleMicroServiceException(MicroServiceException ex) {
        ErrorInfo errorInfo = new ErrorInfo(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    // Other exception handlers...
}
```

## 🔧 Configuration

Set your `clientId` in the application configuration:

```yaml
microservice:
  clientId: YOUR_CLIENT_ID
```

## ❤️ Contributing

Contributions are welcome! Please open issues and submit pull requests for any improvements or bug fixes.

## 📄 License

This project is licensed under the Apache License License.

---

Thank you for using **MicroService Communication Abstraction**! We hope this library simplifies your microservice communication and boosts your productivity. 🚀

---