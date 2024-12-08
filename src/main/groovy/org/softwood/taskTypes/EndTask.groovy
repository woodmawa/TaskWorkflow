package org.softwood.taskTypes

import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@Slf4j
class EndTask {
    String taskName
    CompletableFuture previousTaskOutcome

    CompletableFuture  end () {
        CompletableFuture.completedFuture("completed")
    }

    @Override
    CompletableFuture execute() {
        end ()
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        end ()
    }

    @Override
    Map getTaskVariables() {
        return null
    }
}
