package io.micronaut.starter.cli.feature.linweiyu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityContentCode {
    private String                                 entityContent;
    private List<CodeRenderer.RenderingData.Field> fields;
    private Boolean                                hasVersionField;
}
