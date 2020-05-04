package io.micronaut.starter.generator

import io.micronaut.starter.options.BuildTool
import io.micronaut.starter.options.Language
import io.micronaut.starter.options.TestFramework
import spock.lang.Specification
import spock.lang.Unroll

class LanguageBuildTestFrameworkCombinationsSpec extends Specification {

    @Unroll
    void "#language #buildTool #testFramework combination expected"(Language language, BuildTool buildTool, TestFramework testFramework) {

        expect:
        LanguageBuildTestFrameworkCombinations.combinations().contains([language, buildTool, testFramework])

        where:
        language        | buildTool        | testFramework
        Language.JAVA   | BuildTool.GRADLE | TestFramework.JUNIT
        Language.GROOVY | BuildTool.GRADLE | TestFramework.JUNIT
        Language.KOTLIN | BuildTool.GRADLE | TestFramework.JUNIT
        Language.JAVA   | BuildTool.GRADLE | TestFramework.SPOCK
        Language.GROOVY | BuildTool.GRADLE | TestFramework.SPOCK
        Language.KOTLIN | BuildTool.GRADLE | TestFramework.KOTLINTEST
        Language.JAVA   | BuildTool.MAVEN  | TestFramework.JUNIT
        Language.GROOVY | BuildTool.MAVEN  | TestFramework.JUNIT
        Language.KOTLIN | BuildTool.MAVEN  | TestFramework.JUNIT
        Language.JAVA   | BuildTool.MAVEN  | TestFramework.SPOCK
        Language.GROOVY | BuildTool.MAVEN  | TestFramework.SPOCK
        Language.KOTLIN | BuildTool.MAVEN  | TestFramework.KOTLINTEST
    }

    @Unroll
    void "#language #buildTool #testFramework combination not expected"(Language language, BuildTool buildTool, TestFramework testFramework) {

        expect:
        !LanguageBuildTestFrameworkCombinations.combinations().contains([language, buildTool, testFramework])

        where:
        language        | buildTool        | testFramework
        Language.KOTLIN | BuildTool.GRADLE | TestFramework.SPOCK
        Language.JAVA   | BuildTool.GRADLE | TestFramework.KOTLINTEST
        Language.GROOVY | BuildTool.GRADLE | TestFramework.KOTLINTEST
        Language.KOTLIN | BuildTool.MAVEN  | TestFramework.SPOCK
        Language.JAVA   | BuildTool.MAVEN  | TestFramework.KOTLINTEST
        Language.GROOVY | BuildTool.MAVEN  | TestFramework.KOTLINTEST

    }
}
