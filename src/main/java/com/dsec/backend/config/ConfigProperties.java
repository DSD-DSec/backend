package com.dsec.backend.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.dsec.backend.entity.Tool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@ConfigurationProperties
@Validated
@Getter
@Setter
@ToString
public class ConfigProperties {
    @NotNull
    @Valid
    private JwtProps jwt;
    @NotNull
    @Valid
    private CorsProps cors;
    @NotNull
    @Valid
    private BackendProps backend;
    @NotNull
    @Valid
    private Map<Tool, String> tools;

    @Getter
    @Setter
    @ToString
    public static class JwtProps {
        @NotNull
        private RSAPrivateKey privateKey;
        @NotNull
        private RSAPublicKey publicKey;
        @NotNull
        private Long expiration;
        @NotBlank
        private String cookieName;
    }

    @Getter
    @Setter
    @ToString
    public static class CorsProps {
        @NotBlank
        private String allowOrigins;
    }

    @Getter
    @Setter
    @ToString
    public static class BackendProps {
        @NotBlank
        @URL
        private String url;
        @NotBlank
        private String encryptionKey;
    }
}
