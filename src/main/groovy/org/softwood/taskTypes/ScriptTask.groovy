package org.softwood.taskTypes

import groovy.transform.InheritConstructors
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.taskTypes.secureScriptBase.SecureScriptBase

import java.util.concurrent.CompletableFuture

@InheritConstructors
@Slf4j
class TaskScript extends SecureScriptBase {

    Closure work = {"no-op"}

    //throws SecurityException on validation failure of any user input
    Object run(userInput) {
        log.debug "TaskScript: run the work closure and return result (default is 'done') "
        def result = work.call()
        return result
    }
}

class ScriptEvaluator {

    static Closure evaluateSecure (String scriptText, TaskTrait task ) {
        GroovyShell shell = new GroovyShell()

        shell.setVariable("processVariables", task.parentProcess.processVariables )
        shell.setVariable("taskVariables",task.taskVariables )
        shell.setVariable("task",task )

        // Create a closure from the script text
        Closure scriptClosure = shell.parse(scriptText)

        // Wrap the closure with SecureScriptBase
        Closure secureScript = new SecureScriptBase(scriptClosure)

        // Execute the secure script
        return secureScript()
    }
}

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ScriptTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ScriptTask () {
        taskWork = ScriptTask::runTask //link work to correct do work method
    }

    //TaskScript taskScript = new TaskScript ()
    Closure secureTaskScript = {/* no op*/}


    private void internalSetSecureScript (Closure script) {
       if (script)
           secureTaskScript = script
    }

    void setScript (String scriptText) {
        def task = this
        if (scriptText)
            secureTaskScript = internalSetSecureScript (ScriptEvaluator.evaluateSecure(scriptText, task ))
    }

    //returns completeable future
    private def runTask(Map variables=[:]) {


        //set the task script closure to do something different from default
        secureTaskScript = {String out = "hello William "
            println "~~> process variables for script task are  $processVariables "
            println "~~> change taskVariables from (orig) $taskVariables "
            taskVariables = [something:"was here"]
            println "~~> task script closure running -> " +out + "and taskVariables now $taskVariables"
            return out
        }

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
