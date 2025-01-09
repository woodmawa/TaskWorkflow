package org.softwood.tryout.tryOutTrait.secure

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

class SecureGroovyShell {

    SecureGroovyShell () {
        def secure = new SecureASTCustomizer()
        secure.with {
            closuresAllowed = true
            methodDefinitionAllowed = false
            importsWhitelist = []
            staticImportsWhitelist = []
            staticStarImportsWhitelist = []
            tokensWhitelist = []
            /*constantTypesClassesWhitelist = [
                    Integer, Float, Long, Double, BigDecimal,
                    Boolean, String
            ]
            receiversClassesWhitelist = [
                    Integer, Float, Long, Double, BigDecimal,
                    Boolean, String
            ]*/
        }

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(secure)
        config.scriptBaseClass = 'org.softwood.tryout.secure.BaseScript'

        def shell = new GroovyShell (config)
        shell
    }


}
