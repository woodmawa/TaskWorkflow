package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

class StartTask implements Task {
    String taskName

    CompletableFuture  start () {
        CompletableFuture.completedFuture("started")
    }

    @Override
    CompletableFuture execute() {
        start ()
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        start ()
    }

    @Override
    Map getTaskVariables() {
        return null
    }

    @Override
    String getTaskType() {
        return null
    }
}
