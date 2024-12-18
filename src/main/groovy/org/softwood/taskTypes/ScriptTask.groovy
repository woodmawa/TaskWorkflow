package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Slf4j
class ScriptTask implements TaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    Closure script = {println "hello William"}


    void setScript (Closure script) {
        this.script = script
    }

    CompletableFuture execute() {
        log.info "running script "
        setupTask()
        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {script()}
        closeOutTask()
    }

    CompletableFuture execute(Map taskVariables) {
        log.info "running script with task variables in "
        setupTask()
        taskResult = new CompletableFuture()
        taskResult.supplyAsync {script(new Binding (taskVariables))}
        closeOutTask()
    }


}
