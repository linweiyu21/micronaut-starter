package io.micronaut.starter.cli.feature.linweiyu.entity;

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.micronaut.starter.cli.feature.linweiyu.entity.rule.Annotation;
import io.micronaut.starter.cli.feature.linweiyu.entity.rule.ClassAnnotationRule;
import io.micronaut.starter.cli.feature.linweiyu.entity.rule.ImportRule;
import io.micronaut.starter.cli.feature.linweiyu.entity.util.ResourceReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Code renderer.
 */
public class CodeRenderer {

    /**
     * Renders source code by using Freemarker template engine.
     */
    public static String render(String templatePath, RenderingData data) throws IOException, TemplateException {
        Configuration        config         = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        String               source;
        try (InputStream is = ResourceReader.getResourceAsStream(templatePath);
             BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
            source = buffer.lines().collect(Collectors.joining("\n"));
        }
        templateLoader.putTemplate("template", source);
        config.setTemplateLoader(templateLoader);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setObjectWrapper(new BeansWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS));
        config.setWhitespaceStripping(true);

        try (Writer writer = new StringWriter()) {
            Template template = config.getTemplate("template");
            template.process(data, writer);
            return writer.toString();
        }
    }

    /**
     * Data used when rendering source code.
     */
    @Data
    @NoArgsConstructor
    public static class RenderingData {

        private String packageName;
        private String tableName;
        private String className;
        private String classComment;

        private boolean jpa1Compatible = false;
        private boolean requireJSR305  = false;

        private List<String> topAdditionalCodeList    = new ArrayList<>();
        private List<String> bottomAdditionalCodeList = new ArrayList<>();

        private List<ImportRule>          importRules          = new ArrayList<>();
        private List<ClassAnnotationRule> classAnnotationRules = new ArrayList<>();
        private List<String>              interfaceNames       = new ArrayList<>();
        private List<Field>               fields               = new ArrayList<>();
        private List<Field>               primaryKeyFields     = new ArrayList<>();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Field {
            private String           name;
            private String           columnName;
            private boolean          nullable;
            private String           type;
            private String           comment;
            private String           defaultValue;
            private boolean          primaryKey;
            private boolean          autoIncrement;
            private boolean          primitive;
            private String           generatedValueStrategy;
            private List<Annotation> annotations = new ArrayList<>();

            // linweiyu 的处理
            private List<String>  constraintsAnnotations = new LinkedList<>();
            private Integer       columnSize;
            private CommentEntity commentEntity;
            private Boolean       version                = false;

            @Data
            @NoArgsConstructor
            public static class CommentEntity {
                private String          name;
                private String          initialValue;
                private List<EnumsBean> enums;
                private List<String>    annotations = new LinkedList<>();

                @Data
                @NoArgsConstructor
                public static class EnumsBean implements Serializable {
                    private String name;
                    private String value;
                }
            }
            // linweiyu 的处理
        }

        // linweiyu 的处理
        // 枚举类及其转换类
        private List<EnumClass> enumClasses = new LinkedList<>();

        @Data
        @NoArgsConstructor
        public static class EnumClass {
            String       comment;
            String       propertyEnumName;
            List<String> enums;
        }
        // linweiyu 的处理
    }
}
