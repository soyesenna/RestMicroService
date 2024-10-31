package microservice.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "microservice")
@Component
public class MicroServiceConfig {

    private String clientId;
    private Map<String, String> urls;

    public Map<String, String> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}

