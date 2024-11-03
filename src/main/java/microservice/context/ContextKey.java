package microservice.context;

public enum ContextKey {
  REQUEST_ID("requestId"),
  ROOT_CLIENT_ID("rootClientId"),
  MY_CLIENT_ID("myClientId"),
  META_INFO("metaInfo"),
  HTTP_STATUS("httpStatus"),
  SET_COOKIES("setCookies");

  private String value;

  ContextKey(String value) {
    this.value = value;
  }
}
