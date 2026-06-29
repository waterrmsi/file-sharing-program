package com.glebkrylatov.filesharingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String serverUrl;
    private String adminRealm;
    private String targetRealm;
    private String adminClientId;
    private String adminUsername;
    private String adminPassword;
}