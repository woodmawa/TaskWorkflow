package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@Slf4j
class EndTask implements Task{
    String taskName
    String taskType = this.class.getSimpleName()

    CompletableFuture previousTaskOutcome
    Map<String, ? extends Object> taskVariables

    private CompletableFuture  end () {
        CompletableFuture.completedFuture("completed")
    }

    @Override
    CompletableFuture execute() {
        end()
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        taskVariables ? inputVariables: [:]
        end()  //todo
    }

    @Override
    void setTaskVariables(Map vars) {
        taskVariables = vars?:[:]
    }

    @Override
    Map getTaskVariables() {
        return taskVariables
    }

    @Override
    String getTaskType() {
        return taskType
    }
}
