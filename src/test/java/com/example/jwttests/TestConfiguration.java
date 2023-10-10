package com.example.jwttests;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "jwt-tests", ignoreInvalidFields = false)
@Component
public class TestConfiguration {

    @NotEmpty
    private String connectorBaseUrlTest;

    @NotEmpty
    private String connectorBaseUrlSand;

    @NotEmpty
    private String blurbCoreBaseUrlTest;

    @NotEmpty
    private String blurbCoreBaseUrlSand;

    public String getConnectorBaseUrlTest() {
        return connectorBaseUrlTest;
    }

    public String getConnectorBaseUrlSand() {
        return connectorBaseUrlSand;
    }

    public String getBlurbCoreBaseUrlTest() {
        return blurbCoreBaseUrlTest;
    }

    public String getBlurbCoreBaseUrlSand() {
        return blurbCoreBaseUrlSand;
    }

    public void setConnectorBaseUrlTest(String connectorBaseUrlTest) {
        this.connectorBaseUrlTest = connectorBaseUrlTest;
    }

    public void setConnectorBaseUrlSand(String connectorBaseUrlSand) {
        this.connectorBaseUrlSand = connectorBaseUrlSand;
    }

    public void setBlurbCoreBaseUrlTest(String blurbCoreBaseUrlTest) {
        this.blurbCoreBaseUrlTest = blurbCoreBaseUrlTest;
    }

    public void setBlurbCoreBaseUrlSand(String blurbCoreBaseUrlSand) {
        this.blurbCoreBaseUrlSand = blurbCoreBaseUrlSand;
    }
}
