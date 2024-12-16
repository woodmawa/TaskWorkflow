package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ScriptTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    Closure script = {println "  default: --> hello William"}


    void setScript (Closure script) {
        this.script = script
    }

    CompletableFuture execute() {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running script "
        CompletableFuture taskFuture = new CompletableFuture<>()
        taskFuture.supplyAsync {script()}
        endTime =LocalDateTime.now()
        status = TaskStatus.COMPLETED
        taskFuture
    }

    CompletableFuture execute(Map taskVariables) {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running script with task variables in "
        CompletableFuture taskFuture = new CompletableFuture()
        taskFuture.supplyAsync {script(new Binding (taskVariables))}
        endTime =LocalDateTime.now()
        status = TaskStatus.COMPLETED
        taskFuture
    }

    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }
}
