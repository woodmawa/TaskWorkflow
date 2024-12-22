package org.softwood.taskTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class StartTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    private CompletableFuture  start (Map variables = [:]) {
        taskVariables = variables ?: taskVariables
        if (taskInitialisation)
            taskInitialisation (taskVariables)
        taskResult = CompletableFuture.completedFuture("start task '$taskName' completed")
    }

    @Override
    CompletableFuture execute() {
        taskResourceProcessor (StartTask::start)
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        taskResourceProcessor (StartTask::start, inputVariables)
    }

    @Override
    String getTaskType() {
        taskType
    }
}
