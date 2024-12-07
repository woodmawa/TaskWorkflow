package org.softwood.taskTypes

import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Slf4j
class ScriptTask implements Task {
    String taskName
    CompletableFuture previousTaskOutcome


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    Closure script = {println "hello William"}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING
    ConcurrentHashMap taskVariables = [:]
    String taskType = "Script"

    @Override
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

    @Override
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
