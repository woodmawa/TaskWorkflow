package org.softwood.tryout.tryOutTrait

import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration

class SafeGroovyShell {

    private final GroovyShell shell

    SafeGroovyShell() {
        CompilerConfiguration config = new CompilerConfiguration()
        //use the limitations defined in the SafeScript
        config.scriptBaseClass = SafeScript.class.name

        shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        shell.setVariable("secure", true)
        shell.setVariable("context", "hi william")
        this
    }

    /*Closure getClosure(String closureName) {
        shell.getContext().get(closureName) as Closure
    }*/

    Closure parse (String userScript) {
        Script script = shell.parse(userScript)
        script.&run
    }


}

//implement run()...
@Slf4j
abstract class  SafeScript extends Script{

    SafeScript() {}

    def methodMissing (String name) {
        System.out.println "missing method called $name"
    }

    def propertyMissing (String name) {
        log.info "missing prop $name"
        System.out.println "missing property called '$name'"
    }
    // Override potentially dangerous methods
    void exit(int status) {
        throw new SecurityException("exit() is not allowed")
    }


    /*Object run() {
        log.info "SafeScript.run()"
        Boolean isSecure = this.getBinding().getVariable("secure")
        println "-->is secured by base Script $isSecure"
        return null
    }*/
// Add other restricted methods as needed (e.g., System.gc(), loadLibrary())
}