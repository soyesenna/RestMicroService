package microservice.templates.dtos;

public record ErrorWrapper(
    String errorMessage,
    Integer httpStatusCode,
    Integer errorCode // Optional
) {

}
