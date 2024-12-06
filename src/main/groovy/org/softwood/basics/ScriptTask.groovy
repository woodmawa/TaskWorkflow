package org.softwood.basics

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Slf4j
class ScriptTask implements Task {
    String taskName

    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    Closure script = {println "hello William"}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING
    ConcurrentHashMap taskVariables = [:]
    String taskType = "Script"

    @Override
    Object execute() {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running script "
        var result =  script()
        endTime =LocalDateTime.now()
        status = TaskStatus.COMPLETED
    }

    @Override
    Object execute(Map taskVariables) {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running script with task variables in "
        var result =  script(new Binding (taskVariables))
        endTime =LocalDateTime.now()
        status = TaskStatus.COMPLETED
    }

    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }
}
