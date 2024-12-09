package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

class StartTask implements Task {
    String taskName
    CompletableFuture previousTaskOutcome
    Map<String, ? extends Object> taskVariables = [:]



    CompletableFuture  start () {
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

    @Override
    String getTaskType() {
        return this.class.getSimpleName()
    }
}
