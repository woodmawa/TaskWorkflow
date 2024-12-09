package org.softwood.taskTypes

import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@Slf4j
class EndTask implements Task{
    String taskName
    CompletableFuture previousTaskOutcome
    Map<String, ? extends Object> taskVariables = [:]

    private CompletableFuture  end () {
        CompletableFuture.completedFuture("completed")
    }

    @Override
    CompletableFuture execute() {
        end()
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        end()  //todo
    }

    @Override
    Map getTaskVariables() {
        return taskVariables
    }

    @Override
    String getTaskType() {
        return this.class.getSimpleName()
    }
}
