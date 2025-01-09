package org.softwood.tryout.tryOutTrait.secure

import groovy.lang.Binding
import groovy.lang.GroovyShell
import groovy.lang.Script
import org.codehaus.groovy.control.CompilerConfiguration

class SecureScriptBaseClass extends Script {
    @Override
    Object run() {
        // Implement security measures here
        // This can include regex checks, removing dangerous operations, etc.
        // For simplicity, here's a simple example that restricts System.exit()

        String scriptText = this.getBinding().getVariable("scriptText")

        if (scriptText.contains("System.exit")) {
            throw new SecurityException("Usage of System.exit() is not allowed")
        }

        // Add more security checks as needed
        return null
    }
}

class GptSecureGroovyShell {
    private GroovyShell shell

    GptSecureGroovyShell() {
        CompilerConfiguration config = new CompilerConfiguration()
        //use the limitations defined in the SafeScript
        config.scriptBaseClass = SecureScriptBaseClass.class.name

        shell = new GroovyShell(new Binding(), config)

    }

    Closure parse(String scriptText) {
        // Add additional security checks here
        if (scriptText.contains("System.exit")) {
            throw new SecurityException("Usage of System.exit() is not allowed")
        }

        // Add more security checks to prevent SQL injection, JavaScript, and XSS

        shell.setVariable("scriptText", scriptText)
        Script script = shell.parse(scriptText)



        return script.&run
    }
}

// Usage example
def secureShell = new GptSecureGroovyShell()
def closure = secureShell.parse("""
    // Your secure script here
    println 'Hello, Secure Groovy!'
    System.exit(1)
""")

closure.call()
