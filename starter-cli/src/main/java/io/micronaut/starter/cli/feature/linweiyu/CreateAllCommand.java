/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.cli.feature.linweiyu;

import cn.hutool.core.util.StrUtil;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.util.functional.ThrowingSupplier;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.cli.CodeGenConfig;
import io.micronaut.starter.cli.command.CodeGenCommand;
import io.micronaut.starter.cli.feature.linweiyu.entity.EntityContentCode;
import io.micronaut.starter.cli.feature.linweiyu.entity.EntityContentCodeGenerator;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.template.RenderResult;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.TemplateRenderer;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@CommandLine.Command(name = "create-all", description = "Creates all component for linweiyu")
@Prototype
public class CreateAllCommand extends CodeGenCommand {
    private static final String GENERATED_REPOSITORY_SUFFIX = "GeneratedRepository.groovy";

    private static final List<String> VALID_NO_PKG_ID_TYPES = Arrays.asList("Integer", "Long", "String");

    @ReflectiveAccess
    @CommandLine.Parameters(index = "0", paramLabel = "TABLE-NAME", description = "The name of the table to create")
    String tableName;

    @ReflectiveAccess
    @CommandLine.Parameters(index = "1", paramLabel = "CONFIG-FILE", description = "保存了生成 Entity 代码需要的配置的文件相对路径")
    String configRelativePath = "./src/main/resources/entity-generate.yml";

    @ReflectiveAccess
    @CommandLine.Option(names = {"-i", "--idType"}, description = "Specify custom id type [Integer, Long, String] or full package name [ie. com.corp.Book] - Defaults to Long")
    String idType = "Long";

    private final List<DatabaseDriverFeature> driverFeatures;

    @Inject
    public CreateAllCommand(@Parameter CodeGenConfig config, List<DatabaseDriverFeature> driverFeatures) {
        super(config);
        this.driverFeatures = driverFeatures;
    }

    public CreateAllCommand(CodeGenConfig config,
                            ThrowingSupplier<OutputHandler, IOException> outputHandlerSupplier,
                            ConsoleOutput consoleOutput,
                            List<DatabaseDriverFeature> driverFeatures) {
        super(config, outputHandlerSupplier, consoleOutput);
        this.driverFeatures = driverFeatures;
    }

    @Override
    public boolean applies() {
        return config.getFeatures().contains("data");
    }

    @Override
    public Integer call() throws Exception {
        Project project = getProject(tableName);

        if (config.getSourceLanguage() != Language.GROOVY) {
            throw new IllegalStateException("当前项目使用的不是 Groovy 语言");
        }

        boolean jdbcRepository = config.getFeatures().contains("data-jdbc");// 使用 feature: data-jdbc 来判断 数据库方言 dialect
        String  idTypeImport   = null;

        if (idType.contains(".")) {
            idTypeImport = idType;
        } else if (!VALID_NO_PKG_ID_TYPES.contains(idType)) {
            throw new IllegalArgumentException("Code generation not supported for the specified id type: " + idType + ". Please specify the fully qualified class name.");
        }

        TemplateRenderer templateRenderer = getTemplateRenderer(project);

        String dialect = jdbcRepository ? config.getFeatures()
                                                .stream()
                                                .map(name -> {
                                                    for (DatabaseDriverFeature feature : driverFeatures) {
                                                        if (feature.getName().equals(name)) {
                                                            return feature;
                                                        }
                                                    }
                                                    return null;
                                                })
                                                .filter(Objects::nonNull)
                                                .findFirst()
                                                .map(DatabaseDriverFeature::getDataDialect)
                                                .orElse("MYSQL") : "MYSQL";

        // 实际生成的地方
        {
            // 将类名转换为首字母小写的驼峰命名形式
            final String tablePackageName = StrUtil.lowerFirst(StrUtil.toCamelCase(tableName));

            createBaseRepository(project, idTypeImport, templateRenderer, tablePackageName, configRelativePath);

            createActualRepository(project, templateRenderer, dialect, tablePackageName);

            createService(project, templateRenderer);

            createServiceSpec(project, templateRenderer);
        }

        return 0;
    }

    private void createServiceSpec(Project project, TemplateRenderer templateRenderer) throws Exception {
        RenderResult repositoryRenderResult = templateRenderer.render(new RockerTemplate("src/test/groovy/{packagePath}/service/{className}ServiceSpec.groovy",
                                                                                         linweiyuServiceSpec.template(
                                                                                                 project,
                                                                                                 StrUtil.upperFirst(StrUtil.toCamelCase(project.getPropertyName()))
                                                                                         )),
                                                                      false);
        renderResultProcess(repositoryRenderResult);
    }

    private void createService(Project project, TemplateRenderer templateRenderer) throws Exception {
        RenderResult repositoryRenderResult = templateRenderer.render(new RockerTemplate("src/main/groovy/{packagePath}/service/{className}Service.groovy",
                                                                                         linweiyuService.template(
                                                                                                 project,
                                                                                                 StrUtil.upperFirst(StrUtil.toCamelCase(project.getPropertyName()))
                                                                                         )),
                                                                      false);
        renderResultProcess(repositoryRenderResult);
    }

    /**
     * 生成实际的使用了 @R2dbcRepository 修饰的 Repository 类
     */
    private void createActualRepository(Project project, TemplateRenderer templateRenderer, String dialect, String tablePackageName) throws Exception {
        RenderResult repositoryRenderResult = templateRenderer.render(new RockerTemplate("src/main/groovy/{packagePath}/data/" + tablePackageName + "/{className}.groovy",
                                                                                         linweiyuActualRepository.template(
                                                                                                 project,
                                                                                                 dialect,
                                                                                                 StrUtil.upperFirst(StrUtil.toCamelCase(project.getPropertyName()))
                                                                                         )),
                                                                      false);
        renderResultProcess(repositoryRenderResult);
    }

    /**
     * 生成 GeneratedRepository 类
     */
    private void createBaseRepository(Project project, String idTypeImport, TemplateRenderer templateRenderer, String tablePackageName, String configRelativePath) throws Exception {
        // 生成 entity 的内容体
        final EntityContentCode entityContentCode = EntityContentCodeGenerator.generate(tableName, configRelativePath);
        // 将 entity 内容体中的每一行开头都添加 \t
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(entityContentCode.getEntityContent().getBytes(StandardCharsets.UTF_8))));
        entityContentCode.setEntityContent(reader.lines()
                                                 .map(s -> StrUtil.format("\t\t{}\n", s))
                                                 .reduce((original, s) -> StrUtil.format("{}{}", original, s))
                                                 .orElse(null));


        RenderResult repositoryRenderResult = templateRenderer.render(new RockerTemplate("src/main/groovy/{packagePath}/data/" + tablePackageName + "/{className}" + GENERATED_REPOSITORY_SUFFIX,
                                                                                         linweiyuBaseRepository.template(project,
                                                                                                                         idTypeImport,
                                                                                                                         idType,
                                                                                                                         tablePackageName,
                                                                                                                         StrUtil.upperFirst(StrUtil.toCamelCase(project.getPropertyName())),
                                                                                                                         entityContentCode
                                                                                         )),
                                                                      overwrite);

        renderResultProcess(repositoryRenderResult);
    }

    private void renderResultProcess(RenderResult repositoryRenderResult) throws Exception {
        if (repositoryRenderResult != null) {
            if (repositoryRenderResult.isSuccess()) {
                out("@|blue ||@ Rendered repository to " + repositoryRenderResult.getPath());
            } else if (repositoryRenderResult.isSkipped()) {
                warning("Rendering skipped for " + repositoryRenderResult.getPath() + " because it already exists. Run again with -f to overwrite.");
            } else if (repositoryRenderResult.getError() != null) {
                throw repositoryRenderResult.getError();
            }
        }
    }
}
