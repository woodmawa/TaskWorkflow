package org.softwood.taskTypes

import groovy.transform.InheritConstructors
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.softwood.taskTypes.secureScriptBase.SecureBaseScript

import java.util.concurrent.CompletableFuture


@Slf4j
class SecureScriptEvaluator {

    volatile private GroovyShell shell

    SecureScriptEvaluator () {
        CompilerConfiguration config = new CompilerConfiguration()
        //set the secure base script which does the validations
        config.setScriptBaseClass(SecureBaseScript.class.name)
        shell = new GroovyShell(this.class.classLoader, new Binding(), config)
    }



    Closure parse (String userScriptText, TaskTrait task ) {

        shell.setVariable("processVariables", task.parentProcess.processVariables )
        shell.setVariable("taskVariables",task.taskVariables )
        shell.setVariable("task",task )
        shell.setVariable("secure", true)

        log.debug "parsing userScriptText for forbidden actions task $task.taskName : script : [$userScriptText] "
        // Create a closure from the script text
        Script secureScript
        try { secureScript = shell.parse(userScriptText)

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

    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ScriptTask (String scriptText = null) {
        if (scriptText)
            secureTaskScript = internalSetSecureScript (new SecureScriptEvaluator().evaluateSecure(scriptText, this ))
        taskWork = ScriptTask::runTask
    }


    private void internalSetSecureScript (Closure script) {
       if (script)
           secureTaskScript = script
    }

    void setScript (String scriptText) {
        def task = this
        if (scriptText)
            secureTaskScript = internalSetSecureScript (new SecureScriptEvaluator().evaluateSecure(scriptText, task ))
    }

    //returns completeable future
    private def runTask(Map variables=[:]) {

        taskResult = new CompletableFuture<>()

        //task result is future of computed outcome or the exception itself
        taskResult.completeAsync {
            try {
                log.debug "Script's runTask:  running taskScript and get its Future result "
                secureTaskScript.call()        //call the secure task script closure ...
            } catch (Exception ex) {
                ex
            }
        }

    }

}
