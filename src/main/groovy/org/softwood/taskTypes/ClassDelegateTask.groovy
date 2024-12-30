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
        taskWork = ClassDelegateTask::runTask //link work to correct do work method


    }

    private def runTask(Map taskVariables=[:]) {
        taskResult = new CompletableFuture<>()
        taskResult.supplyAsync {taskDelegateFunction(new Binding (taskVariables))}
    }


}
