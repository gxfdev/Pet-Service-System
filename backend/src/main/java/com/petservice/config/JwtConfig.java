package com.petservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "pet_service_jwt_secret_key_2024_please_change_in_production";
    private Long expiration = 86400000L;
    private Long refreshExpiration = 604800000L;

    public String getSecret() { return secret; } public void setSecret(String secret) { this.secret = secret; }
    public Long getExpiration() { return expiration; } public void setExpiration(Long expiration) { this.expiration = expiration; }
    public Long getRefreshExpiration() { return refreshExpiration; } public void setRefreshExpiration(Long refreshExpiration) { this.refreshExpiration = refreshExpiration; }
}
