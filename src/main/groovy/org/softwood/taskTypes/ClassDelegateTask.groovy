package org.softwood.taskTypes


import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture
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

    private def run(Map taskVariables=[:]) {
        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {taskDelegateFunction(new Binding (taskVariables))}
    }

    CompletableFuture execute() {
        log.info "running class delegate "

        taskResourceProcessor (ClassDelegateTask::run)

     }

     CompletableFuture execute(Map taskVariables) {
         log.info "running script with task variables in "
         taskResourceProcessor (ClassDelegateTask::run, taskVariables)

    }


}
