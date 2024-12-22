package org.softwood.taskTypes

import groovy.transform.InheritConstructors
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@InheritConstructors
class TaskScript extends Script {

    Closure script = {}

    @Override
    Object run() {
        def result = script.call()
        println "found binding var " + var
        return result
    }
}

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ScriptTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    TaskScript taskScript = new TaskScript ()




    void setScript (Closure script) {
       taskScript.script = script
    }

    private def run(Map taskVariables=[:]) {
        taskScript.script = {String out = "hello William "
            var = [something:"was here"]
            println "task script running -> " +out
            return out
        }

        taskResult = new CompletableFuture<>()
        Binding scriptBinding = new Binding ()
        scriptBinding.setVariable("var",taskVariables ?: ["try":"out"] )
        taskScript.setBinding (scriptBinding)
        log.info "setting binding on script with var as : " + scriptBinding.variables

        taskResult.completeAsync {
            try {
                taskScript.run()
            }catch (Exception ex) {
                ex.toString()
            }
        }

    }


    CompletableFuture execute() {
        log.info "running scriptTask Script  "
        taskResourceProcessor (ScriptTask::run)
    }

    CompletableFuture execute(Map taskVariables) {
        log.info "running script with task variables in "
        taskVariables ?: [:]
        taskResourceProcessor (ScriptTask::run, taskVariables)


    }


}
