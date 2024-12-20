package org.softwood.taskTypes


import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@Slf4j
class ScriptTask implements TaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    Closure script = {println "hello William"}


    void setScript (Closure script) {
        this.script = script
    }

    def run(Map taskVariables=[:]) {
        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {script(new Binding (taskVariables))}
    }


    CompletableFuture execute() {
        log.info "running script "
        taskResourceProcessor (ScriptTask::run)

        /*setupTask()
        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {script()}
        closeOutTask()*/
    }

    CompletableFuture execute(Map taskVariables) {
        log.info "running script with task variables in "
        taskVariables ?: [:]
        taskResourceProcessor (ScriptTask::run, taskVariables)


    }


}
