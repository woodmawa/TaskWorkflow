package org.softwood.taskTypes.secureScriptBase

import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.transform.ASTTransformation
import org.softwood.taskTypes.TaskTrait
import org.codehaus.groovy.syntax.Types


@Slf4j
class SecureScriptEvaluator {

    volatile private GroovyShell shell

    private Set<String> forbiddenMethods = [
            'System.exit',
            'Runtime.exec',
            'ProcessBuilder',
            'System.getRuntime',
            'SecurityManager',
            'ClassLoader',
            'URLClassLoader'
    ] as Set

    private Set<String> forbiddenProperties = [
            'config'

    ] as Set

    private ValidationDelegate delegate = new ValidationDelegate(forbiddenMethods, forbiddenProperties)


    SecureScriptEvaluator () {

        List allowedTokensList = Types.collect{it.properties.collect{it.key}  }.flatten()

        CompilerConfiguration config = new CompilerConfiguration()
        //set the secure base script which does the validations
        config.setScriptBaseClass(DelegatingScript.class.name)

        // Add secure imports to prevent access to System class

        //check for anyone calling system exit in script - and through an exception
        SystemExitASTTransformation systemExitASTTransformation = new SystemExitASTTransformation()
        config.addCompilationCustomizers(
                new ASTTransformationCustomizer(systemExitASTTransformation)
        )

        config.addCompilationCustomizers(new SecureASTCustomizer().tap {
            closuresAllowed = true
            methodDefinitionAllowed = true
            //indirectImportCheckEnabled = true

            // Explicitly define allowed classes/packages
            allowedImports = [
                    'java.lang.Math',
                    'java.lang.String',
                    'java.util.List',
                    'java.util.Map',
                    'java.util.*',
                    'java.util.concurrent.*',
                    'java.time.*',
                    'groovy.util.logging.*'
                    // Add other safe classes you want to allow
            ]

            // Explicitly deny dangerous classes
            /*disallowedImports = [
                    'java.lang.System',
                    'java.lang.Runtime',
                    'groovy.lang.GroovyShell',
                    'java.io.File'
                    // Add other classes you want to block
            ]*/

            // Optionally disable specific statements cant fnd token
            List defaultAllowedTokens = allowedTokensList
            //allowedTokens = allowedTokensList
                    /*[
                    PLUS,PLUS_EQUAL,PLUS_PLUS,
                    EQUAL,EQUALS, PLUS_EQUAL,
                    MINUS, MINUS_EQUAL, MINUS_MINUS,
                    MATCH_REGEX,
                    MOD, MOD_EQUAL,


            //Types.PACKAGE,
                    //Types.CLASS_DEF
                    // Add other tokens you want to block - cant find defintion of these
            ]*/

            // Prevent static method calls if needed
            //allowedStaticImports = []
            disallowedStaticImports = ['java.lang.System']


        })


        shell = new GroovyShell(this.class.classLoader, new Binding(), config)
    }



    Closure parse (String userScriptText, TaskTrait task ) {

        shell.setVariable("processVariables", task.parentProcess.processVariables )
        shell.setVariable("taskVariables",task.taskVariables )
        shell.setVariable("task",task )
        shell.setVariable("secure", true)

        log.debug "parsing userScriptText for forbidden actions task $task.taskName : script : [$userScriptText] "
        // Create a closure from the script text
        DelegatingScript secureScript
        try {
            secureScript = (DelegatingScript) shell.parse(userScriptText)
            secureScript.setDelegate (delegate)
            secureScript
        }
        catch (CompilationFailedException ce) {
            log.error "ScriptText is invalid: ${ce.message}"
        }

        // return the parsed script as a closure
        return secureScript::run
    }

}
