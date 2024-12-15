package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction

@Slf4j
class ClassDelegateTask implements TaskTrait {
    String taskType = this.class.getSimpleName()
    String taskNature = "task"

    Closure taskDelegateFunction  //class::bifunc(Map param )


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ClassDelegateTask (String name, BiFunction methodRef ) {
        taskName = name
        taskDelegateFunction = methodRef


    }

    @Override
    CompletableFuture execute() {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING
        log.info "running class delegate "

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


}
