package org.softwood.taskTypes

import groovy.transform.ToString

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class StartTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    CompletableFuture  start () {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING

         if (taskInitialisation)
            taskInitialisation (taskVariables)
        //let  process instance know that start step is complete

        endTime = LocalDateTime.now()
        status = TaskStatus.COMPLETED
        CompletableFuture.completedFuture("start task completed")
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
    String getTaskType() {
        taskType
    }
}
