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
    TaskCategories taskCategory = TaskCategories.Task

    Closure taskDelegateFunction  //class::bifunc(Map param )


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ClassDelegateTask (String name, BiFunction methodRef ) {
        taskName = name
        taskDelegateFunction = methodRef


    }

    CompletableFuture execute() {
        log.info "running class delegate "
        setupTask()

        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {taskDelegateFunction()}
        closeOutTask()

     }

     CompletableFuture execute(Map taskVariables) {
         log.info "running script with task variables in "
         setupTask()
         taskResult = new CompletableFuture()
         taskResult.supplyAsync {taskDelegateFunction(taskVariables)}
         closeOutTask()
    }


}
