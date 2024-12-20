package org.softwood.taskTypes

import groovy.transform.ToString

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class StartTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    CompletableFuture  start () {
        if (taskInitialisation)
            taskInitialisation (taskVariables)
        //let  process instance know that start step is complete

        taskResult = CompletableFuture.completedFuture("start task completed")
    }

    CompletableFuture  start (Map variables) {
        taskVariables = variables ?: taskVariables
        start()
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
