package org.softwood.tryout.tryOutTrait.secure

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

//def shell = new SecureGroovyShell()

def createSecureGroovyShell() {
    def secure = new SecureASTCustomizer()
    secure.with {
        closuresAllowed = true
        methodDefinitionAllowed = false
        importsWhitelist = []
        staticImportsWhitelist = []
        staticStarImportsWhitelist = []
        tokensWhitelist = []
        constantTypesClassesWhitelist = [
                Integer, Float, Long, Double, BigDecimal,
                Boolean, String
        ]
        receiversClassesWhitelist = [
                Integer, Float, Long, Double, BigDecimal,
                Boolean, String
        ]
    }

    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(secure)
    config.scriptBaseClass = 'org.softwood.tryout.tryOutTrait.secure.BaseScript'

    def shell = new GroovyShell(config)
    return shell
}

def shell = createSecureGroovyShell()
def script = "return { -> println 'Hello' }"
def closure = shell.parse(script).&run
closure()