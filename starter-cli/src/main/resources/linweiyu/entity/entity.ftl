<#list fields as field>
<#-- 字段描述 -->
<#if field.comment?has_content>
/**
 * ${field.commentEntity.name}
 */
</#if>
<#-- 字段描述 -->
<#-- 主键处理 -->
<#if field.primaryKey>
@Id
</#if>
<#if field.autoIncrement>
@GeneratedValue
</#if>
<#-- 主键处理 -->
<#-- field 的注解列表处理 -->
<#list field.annotations as annotation>
${annotation.toString()}
</#list>
<#-- field 的注解列表处理 -->
<#-- 列名映射注解 -->
@Column(name = "${field.columnName}", nullable = ${field.nullable?c})
<#-- 列名映射注解 -->
<#-- constraintsAnnotations 注解输出 -->
<#if field.constraintsAnnotations??>
<#list field.constraintsAnnotations as constraintsAnnotation>
${constraintsAnnotation.toString()}
</#list>
</#if>
<#-- constraintsAnnotations 注解输出 -->
<#-- comment 中的注解输出 -->
<#if field.commentEntity?? && field.commentEntity.annotations??>
<#list field.commentEntity.annotations as commentEntityAnnotation>
${commentEntityAnnotation}
</#list>
</#if>
<#-- comment 中的注解输出 -->
<#-- 实际的属性声明 -->
${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>
<#-- 实际的属性声明 -->
<#-- 判断是否下一行, 有则输出一个空行 -->
<#if field?has_next>

</#if>
<#-- 判断是否下一行, 有则输出一个空行 -->
</#list>
<#list enumClasses as enumClass>

${enumClass.comment}
static enum ${enumClass.propertyEnumName} {
    <#list enumClass.enums as enum>
    ${enum}<#if enum?has_next>,</#if>
    </#list>

    @Singleton
    @CompileStatic
    static class ${enumClass.propertyEnumName}AttributeConverter implements AttributeConverter<${enumClass.propertyEnumName}, Integer> {
        Integer convertToPersistedValue(@Nullable ${enumClass.propertyEnumName} entityValue, @NonNull ConversionContext context) { entityValue?.ordinal() }

        ${enumClass.propertyEnumName} convertToEntityValue(@Nullable Integer persistedValue, @NonNull ConversionContext context) {
            (persistedValue == null ?: ${enumClass.propertyEnumName}.enumConstants.find { it.ordinal() == persistedValue }) as ${enumClass.propertyEnumName}
        }
    }
}
</#list>
