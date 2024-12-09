package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

class StartTask implements Task {
    String taskName
    CompletableFuture previousTaskOutcome
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation


    CompletableFuture  start () {

        //todo - get next step from template
        //if taskInitialisation -> call taskInitialisation ( taskVariables)
        //let  process instance know that start step is complete
        CompletableFuture.completedFuture("started")
    }

    @Override
    CompletableFuture execute() {
        start ()
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        taskVariables = inputVariables
        start ()
    }

    @Override
    Map getTaskVariables() {
        return taskVariables.asImmutable()
    }

    Void addTaskVariables(Map variables ) {
        return taskVariables << variables ?: [:]
    }

    @Override
    String getTaskType() {
        return this.class.getSimpleName()
    }
}
