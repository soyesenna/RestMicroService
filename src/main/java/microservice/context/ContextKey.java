package microservice.context;

public enum ContextKey {
  REQUEST_ID("requestId"),
  CLIENT_ID("clientId");

  private String value;

  ContextKey(String value) {
    this.value = value;
  }
}
