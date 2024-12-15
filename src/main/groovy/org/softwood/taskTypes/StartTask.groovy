package org.softwood.taskTypes


import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture


class StartTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    String taskNature = "task"

    CompletableFuture  start () {
        startTime = LocalDateTime.now()
        status = TaskStatus.RUNNING

         if (taskInitialisation)
            taskInitialisation (taskVariables)
        //let  process instance know that start step is complete

        endTime = LocalDateTime.now()
        status = TaskStatus.COMPLETED
        CompletableFuture.completedFuture("start task started")
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
