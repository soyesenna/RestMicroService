package microservice.templates.enums;

public enum Environments {
  CLIENT_ID("client_id");

  private String name;

  Environments(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
