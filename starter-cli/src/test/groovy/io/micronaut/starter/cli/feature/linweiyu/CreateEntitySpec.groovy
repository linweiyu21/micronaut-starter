package io.micronaut.starter.cli.feature.linweiyu

import groovy.util.logging.Slf4j
import io.micronaut.starter.cli.feature.linweiyu.entity.EntityContentCodeGenerator
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class CreateEntitySpec extends Specification {
    
    @Unroll
    void "test creating entity"() {
        when:
            def result = EntityContentCodeGenerator.generate('robot')
        then:
            result
            println '---'
            print result
            println '---'
    }
    
}
