package io.micronaut.starter.cli.feature.linweiyu.entity.config;

import lombok.Data;

import java.io.Serializable;

/**
 * JDBC connection settings.
 */
@Data
public class EntityGenerateSettings implements Serializable {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schemaPattern;
}