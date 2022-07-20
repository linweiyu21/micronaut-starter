package io.micronaut.starter.cli.feature.linweiyu


import io.micronaut.context.ApplicationContext
import io.micronaut.starter.cli.CodeGenConfig
import io.micronaut.starter.cli.CommandFixture
import io.micronaut.starter.cli.CommandSpec
import io.micronaut.starter.feature.database.DatabaseDriverFeature
import io.micronaut.starter.io.ConsoleOutput
import io.micronaut.starter.options.BuildTool
import io.micronaut.starter.options.Language
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Unroll

class CreateAllSpec extends CommandSpec implements CommandFixture {
    
    @Shared
    @AutoCleanup
    ApplicationContext beanContext = ApplicationContext.run()
    
    @Unroll
    void "test creating a jdbc repository - #language.getName()"(Language language) {
        generateProject(language, BuildTool.GRADLE, ['data-jdbc'])
        CodeGenConfig codeGenConfig = CodeGenConfig.load(beanContext, dir, ConsoleOutput.NOOP)
        ConsoleOutput consoleOutput = Mock(ConsoleOutput)
        CreateAllCommand command = new CreateAllCommand(codeGenConfig, getOutputHandler(consoleOutput), consoleOutput, beanContext.getBeansOfType(DatabaseDriverFeature).toList())
        command.tableName = entityName
        
        when:
            Integer exitCode = command.call()
            File generatedRepositoryFile = new File(dir,
                                                    language.getSourcePath("/example/micronaut/data/${entityName}/${entityName[0].toUpperCase()}${entityName[1..-1]}GeneratedRepository"))
            File actualRepositoryFile = new File(dir, language.getSourcePath("/example/micronaut/data/${entityName}/${entityName[0].toUpperCase()}${entityName[1..-1]}"))
            File serviceFile = new File(dir, language.getSourcePath("/example/micronaut/service/${entityName[0].toUpperCase()}${entityName[1..-1]}Service"))
        
        then:
            exitCode == 0
            generatedRepositoryFile.exists()
            
            println "生成的 generatedRepository 文件地址: ${generatedRepositoryFile.absolutePath}"
            println "生成的 actualRepository 文件地址: ${actualRepositoryFile.absolutePath}"
            println "生成的 service 文件地址: ${serviceFile.absolutePath}"
            "code ${generatedRepositoryFile.absolutePath}".execute()
            "code ${actualRepositoryFile.absolutePath}".execute()
            "code ${serviceFile.absolutePath}".execute()
            
            generatedRepositoryFile.text.contains('ReactorPageableRepository')
            
            3 * consoleOutput.out({ it.contains("Rendered repository") })
        
        where:
            language << Language.GROOVY
            entityName = 'robot'
    }
    
}
