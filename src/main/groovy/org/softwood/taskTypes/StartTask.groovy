package org.softwood.taskTypes

import groovy.transform.MapConstructor

import java.util.concurrent.CompletableFuture


class StartTask implements Task {
    String taskName
    String taskType = this.class.getSimpleName()

    CompletableFuture previousTaskOutcome
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation = {var ->}


    CompletableFuture  start () {

        //todo - get next step from template
        if (taskInitialisation)
            taskInitialisation (taskVariables)
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
    void setTaskVariables(Map vars) {
        taskVariables = vars?: [:]
    }

    @Override
    Map getTaskVariables() {
        return taskVariables.asImmutable()
    }


    Void addTaskVariables(Map variables ) {
        taskVariables << variables ?: [:]
    }

    @Override
    String getTaskType() {
        taskType
    }
}
