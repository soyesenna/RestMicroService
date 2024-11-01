package microservice.exception;

import java.util.List;
import microservice.templates.dtos.ErrorWrapper;
import org.springframework.http.HttpMethod;

public class MicroServiceRequestFailException extends MicroServiceException {

  private List<String> errorStack;
  private HttpMethod httpMethod;
  private String requestUrl;
  private ErrorWrapper errorWrapper;
  private String serviceName;

  public MicroServiceRequestFailException(List<String> errorStack, HttpMethod httpMethod, String url, ErrorWrapper errorWrapper, String serviceName) {
    super("MicroService Request is received fail response", errorWrapper.httpStatusCode());
    this.errorStack = errorStack;
    this.httpMethod = httpMethod;
    this.requestUrl = url;
    this.errorWrapper = errorWrapper;
    this.serviceName = serviceName;
  }

  public List<String> getErrorStack() {
    return errorStack;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public ErrorWrapper getErrorInfo() {
    return errorWrapper;
  }

  public String getServiceName() {
    return serviceName;
  }
}
