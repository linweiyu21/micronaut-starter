package io.micronaut.starter.cli.feature.linweiyu.entity.rule;

import lombok.Data;

import java.io.Serializable;

/**
 * Represents an attribute of a Java annotation.
 */
@Data
public class AnnotationAttribute implements Serializable {

    private String name;
    private String value;
    private String code;
}
