package org.softwood.tryout.tryOutTrait

import org.codehaus.groovy.control.CompilerConfiguration

class SafeGroovyShell {

    private final GroovyShell shell

    SafeGroovyShell(String baseScript) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = "SafeScript"

        shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        shell.evaluate(baseScript)
    }

    Closure getClosure(String closureName) {
        shell.getContext().get(closureName) as Closure
    }

    Object evaluate(String script) {
        shell.evaluate(script)
    }

    static class SafeScript {
        // Override potentially dangerous methods
        static void exit(int status) {
            throw new SecurityException("exit() is not allowed")
        }

        // Add other restricted methods as needed (e.g., System.gc(), loadLibrary())
    }
}