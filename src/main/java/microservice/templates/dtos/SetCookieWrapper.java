package microservice.templates.dtos;

public record SetCookieWrapper(
    String name,
    String value,
    Long maxAge
) {

}
