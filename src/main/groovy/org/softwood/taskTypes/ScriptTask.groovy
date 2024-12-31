package org.softwood.taskTypes

import groovy.transform.InheritConstructors
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@InheritConstructors
@Slf4j
class TaskScript extends Script {

    Closure work = {"no-op"}

    @Override
    Object run() {
        log.debug "TaskScript: run the work closure and return result (default is 'done') "
        def result = work.call()
        return result
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

    TaskScript taskScript = new TaskScript ()

    void setScript (Closure script) {
       if (script)
           taskScript.work = script
    }

    private def runTask(Map variables=[:]) {

        //set the task script closure to do something different from default
        taskScript.work = {String out = "hello William "
            println "~~> process variables for script task are  $processVariables "
            println "~~> change taskVariables from (orig) $taskVariables "
            taskVariables = [something:"was here"]
            println "~~> task script closure running -> " +out + "and taskVariables now $taskVariables"
            return out
        }

        taskResult = new CompletableFuture<>()
        Binding scriptBinding = new Binding ()
        //set the current task and task variables into the script binding
        scriptBinding.setVariable("processVariables", this.parentInstance.processVariables )
        scriptBinding.setVariable("taskVariables",taskVariables )
        scriptBinding.setVariable("task",this )

        taskScript.setBinding (scriptBinding)

        //task result is future of computed outcome or the exception itself
        taskResult.completeAsync {
            try {
                log.debug "Script's runTask:  running taskScript and get its Future result "
                taskScript.run()        //call the script task ...
            }catch (Exception ex) {
                ex
            }
        }

    }

}
