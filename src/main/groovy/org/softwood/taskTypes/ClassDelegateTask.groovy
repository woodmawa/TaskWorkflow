package org.softwood.taskTypes

import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction

@Slf4j
class ClassDelegateTask implements Task {
    String taskName
    Closure taskDelegateFunction  //class::bifunc(Map param )
    CompletableFuture previousTaskOutcome


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING
    ConcurrentHashMap taskVariables = [:]
    String taskType = "ClassDelegate"

    ClassDelegateTask (String name, BiFunction methodRef ) {
        taskName = name
        taskDelegateFunction = methodRef


    }

    @Override
    CompletableFuture execute() {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running script "
        CompletableFuture taskFuture = new CompletableFuture<>()
        taskFuture.supplyAsync {taskDelegateFunction()}
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
        taskFuture.supplyAsync {taskDelegateFunction(taskVariables)}
        endTime =LocalDateTime.now()
        status = TaskStatus.COMPLETED
        taskFuture
    }

    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }
}
