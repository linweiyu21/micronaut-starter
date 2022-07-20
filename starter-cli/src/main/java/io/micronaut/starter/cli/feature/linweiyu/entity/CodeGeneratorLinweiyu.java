package io.micronaut.starter.cli.feature.linweiyu.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.micronaut.starter.cli.feature.linweiyu.entity.metadata.Column;
import io.micronaut.starter.cli.feature.linweiyu.entity.rule.Annotation;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class CodeGeneratorLinweiyu {
    private static final String VERSION_NAME       = "version";
    private static final String VERSION_ANNOTATION = "@Version";

    private static final String SIZE_ANNOTATION = "@Size(max = {}, message = \"{} 长度不能超过 {}\")";

    private static final String NULLABLE_ANNOTATION = "@Nullable";
    private static final String NOT_NULL_ANNOTATION = "@NotNull";

    private static final String MAPPEDPROPERTY_ANNOTATION = "@MappedProperty(converter = {}.{}AttributeConverter.class)";

    // 包含这些注解的字段, 被修饰为 @Nullable
    private static final List<String> NULLABLE_FIELD_ANNOTATIONS = Lists.newArrayList("@Version", "@DateUpdated", "@DateCreated");

    // 用于 commentEntity 中的 initialValue 字段, 用来标识字段的默认值为空字符串
    private static final String INITIAL_VALUE_BLANK_STRING       = "BLANK_STRING";
    private static final String INITIAL_VALUE_BLANK_STRING_VALUE = "''";

    static void process(String tableName, CodeRenderer.RenderingData data, Column c, CodeRenderer.RenderingData.Field f) {
        // 计算属性的枚举类型名称
        final String propertyEnumName = StrUtil.format("{}{}",
                                                       StrUtil.upperFirst(tableName), StrUtil.upperFirst(f.getName()));

        // 判断是否要添加空注解
        {
            if (data.isRequireJSR305() && !f.isPrimitive() && !f.isPrimaryKey()) {
                if (f.isNullable()) {
                    f.getConstraintsAnnotations().add(NULLABLE_ANNOTATION);
                } else {
                    f.getConstraintsAnnotations().add(NOT_NULL_ANNOTATION);
                }
            }
        }

        // 将 io.micronaut.starter.cli.feature.linweiyu.entity.CodeRenderer.RenderingData.Field.annotations 字段的值
        // 转移到 io.micronaut.starter.cli.feature.linweiyu.entity.CodeRenderer.RenderingData.Field.constraintsAnnotations
        {
            f.getConstraintsAnnotations()
             .addAll(f.getAnnotations()
                      .stream()
                      .map(Annotation::toString)
                      .collect(toList()));
        }

        // 获取字段长度
        {
            f.setColumnSize(c.getColumnSize());
        }

        // 生成 commentEntity
        {
            c.getDescription().ifPresent((String description) -> {
                final CodeRenderer.RenderingData.Field.CommentEntity commentEntity = JSONUtil.toBean(description, CodeRenderer.RenderingData.Field.CommentEntity.class);
                f.setCommentEntity(commentEntity);
            });
            assert f.getCommentEntity() != null;
        }

        // 为 String 类型字段添加 @Size 注解
        {
            if (f.getType().equals("String") && Objects.nonNull(f.getColumnSize())) {
                // 从 comment 中获取字段中文名称
                f.getConstraintsAnnotations()
                 .add(StrUtil.format(SIZE_ANNOTATION,
                                     f.getColumnSize(), f.getCommentEntity().getName(), f.getColumnSize()));
            }
        }

        // 判断字段是否拥有 @Version 注解修饰的字段 且 字段名称为 version
        {
            if (CollUtil.isNotEmpty(f.getCommentEntity().getAnnotations())) {
                f.getCommentEntity()
                 .getAnnotations()
                 .stream()
                 .filter(a -> StrUtil.equals(a, VERSION_ANNOTATION) && StrUtil.equals(f.getName(), VERSION_NAME))
                 .findAny()
                 .ifPresent(a -> f.setVersion(true));
            }
        }

        // 为枚举类型添加默认值
        {
            if (StrUtil.isNotBlank(f.getCommentEntity().getInitialValue())) {
                if (CollUtil.isNotEmpty(f.getCommentEntity().getEnums())) {
                    f.setDefaultValue(StrUtil.format("{}.{}", propertyEnumName, f.getCommentEntity().getInitialValue()));
                }
            }
        }

        // 为普通类型添加默认值
        {
            if (StrUtil.isNotBlank(f.getCommentEntity().getInitialValue())) {
                if (StrUtil.equals(f.getCommentEntity().getInitialValue(), INITIAL_VALUE_BLANK_STRING)) {
                    f.setDefaultValue(INITIAL_VALUE_BLANK_STRING_VALUE);
                } else {
                    f.setDefaultValue(f.getCommentEntity().getInitialValue());
                }
            }
        }

        // 处理拥有 enums 值的字段
        {
            if (CollUtil.isNotEmpty(f.getCommentEntity().getEnums())) {
                // 将字段名称修改为 {表名}{属性名}
                {
                    f.setType(propertyEnumName);
                }

                // 添加 io.micronaut.data.annotation.MappedProperty 注解
                {
                    final String mappedPropertyAnnotation = StrUtil.format(MAPPEDPROPERTY_ANNOTATION,
                                                                           propertyEnumName, propertyEnumName);
                    f.getCommentEntity().getAnnotations().add(mappedPropertyAnnotation);
                }

                // 添加对应的枚举类和转换类
                {
                    final CodeRenderer.RenderingData.EnumClass enumClass = new CodeRenderer.RenderingData.EnumClass();
                    enumClass.setPropertyEnumName(propertyEnumName);
                    enumClass.setComment(f.getComment());
                    enumClass.setEnums(f.getCommentEntity()
                                        .getEnums()
                                        .stream()
                                        .map(CodeRenderer.RenderingData.Field.CommentEntity.EnumsBean::getName)
                                        .collect(toList())
                    );
                    data.getEnumClasses().add(enumClass);
                }
            }
        }

        // io.micronaut.starter.cli.feature.linweiyu.entity.CodeRenderer.RenderingData.Field.CommentEntity.annotations 中包含了 [@Version, @DateUpload, @DateCreated] 注解的字段, 将其 @NotNull 改成 @Nullable
        {
            if (CollUtil.containsAny(f.getCommentEntity().getAnnotations(), NULLABLE_FIELD_ANNOTATIONS)) {
                f.getConstraintsAnnotations().remove(NOT_NULL_ANNOTATION);
                f.getConstraintsAnnotations().add(NULLABLE_ANNOTATION);
            }
        }
    }
}
