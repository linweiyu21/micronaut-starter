package io.micronaut.starter.cli.feature.linweiyu.entity.rule;

import lombok.Data;

import java.io.Serializable;

/**
 * Rule used to determine the Java class name for an entity.
 */
@Data
public class ClassNameRule implements Serializable {

    private String tableName;
    private String className;
}
