package org.softwood.taskTypes

import groovy.transform.MapConstructor

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture


class StartTask implements TaskTrait {
    String taskType = this.class.getSimpleName()

    CompletableFuture  start () {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING

         if (taskInitialisation)
            taskInitialisation (taskVariables)
        //let  process instance know that start step is complete

        endTime = LocalDateTime.now()
        status = TaskStatus.COMPLETED
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
    String getTaskType() {
        taskType
    }
}
