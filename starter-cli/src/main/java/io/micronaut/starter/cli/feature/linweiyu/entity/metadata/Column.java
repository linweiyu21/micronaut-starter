package io.micronaut.starter.cli.feature.linweiyu.entity.metadata;

import lombok.Data;

import java.util.Optional;

/**
 * Database metadata: a column in a table
 */
@Data
public class Column {

    private String name;
    private int typeCode;
    private String typeName;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private Optional<String> description = Optional.empty();

    private int columnSize;
}
