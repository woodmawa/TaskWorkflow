package org.softwood.taskTypes


import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.softwood.taskTypes.secureScriptBase.ValidationDelegate

import java.util.concurrent.CompletableFuture


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
        CompilerConfiguration config = new CompilerConfiguration()
        //set the secure base script which does the validations
        //config.setScriptBaseClass(SecureBaseScript.class.name)
        config.setScriptBaseClass(DelegatingScript.class.name)

        // Add secure imports to prevent access to System class
        config.addCompilationCustomizers(new SecureASTCustomizer().tap {
            // Explicitly define allowed classes/packages
            importsWhitelist = [
                    'java.lang.Math',
                    'java.lang.String',
                    'java.util.List',
                    'java.util.Map'
                    // Add other safe classes you want to allow
            ]

            // Explicitly deny dangerous classes
            importsBlacklist = [
                    'java.lang.System',
                    'java.lang.Runtime',
                    'groovy.lang.GroovyShell',
                    'java.io.File'
                    // Add other classes you want to block
            ]

            // Optionally disable specific statements cant fnd token
            /*tokensBlacklist = [
                    Token.PACKAGE,
                    Token.CLASS_DEF
                    // Add other tokens you want to block
            ]*/

            // Prevent static method calls if needed
            //staticImportsWhitelist = []
            staticImportsBlacklist = ['java.lang.System']

            // Disallow star imports
            starImportsWhitelist = []
            starImportsBlacklist = ['java.lang.*', 'java.io.*']
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

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ScriptTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task
    Closure secureTaskScript = {/* no op*/}

    String copyOfScriptText
    RuntimeException scriptException

    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ScriptTask (String scriptText = null) {
        taskWork = ScriptTask::runTask
    }


    private void internalSetSecureScript (Closure script) {
       if (script)
           secureTaskScript = script
    }

    void setScript (String scriptText) {
        def task = this
        if (scriptText)
            secureTaskScript = internalSetSecureScript (new SecureScriptEvaluator().parse(scriptText, task ))
    }

    //returns completeable future
    private def runTask(Map variables=[:]) {

        taskResult = new CompletableFuture<>()

        //task result is future of computed outcome or the exception itself
        taskResult.completeAsync {
            try {
                log.debug "Script's runTask:  running taskScript and get its Future result "
                secureTaskScript.call()        //call the secure task script closure ...
            } catch (SecurityException | MultipleCompilationErrorsException | Exception ex) {
                log.info "script task $taskName threw exception  : " + ex.stackTrace
                this.status = TaskStatus.EXCEPTION
                scriptException = ex
                return ex
            }
        }

    }

}
