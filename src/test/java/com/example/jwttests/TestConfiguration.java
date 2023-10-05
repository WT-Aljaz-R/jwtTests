package com.example.jwttests;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "blurb-core", ignoreInvalidFields = false)
@Component
public class TestConfiguration {
}
