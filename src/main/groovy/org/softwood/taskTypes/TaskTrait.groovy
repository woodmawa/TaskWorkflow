package org.softwood.taskTypes

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

trait TaskTrait implements  Task {
    String taskName
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation = {var ->}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING

    List<List> previousTaskResults = []

    @Override
    void setPreviousTaskResults (Task previousTask, CompletableFuture result) {
        previousTaskResults << [previousTask,result]
    }

    //@Override
    void setTaskName (String name) {
        taskName = name
    }

    //@Override
    String getTaskName () {
        taskName
    }

    @Override
    void setTaskVariables(Map vars) {
        taskVariables = vars?:[:]
    }

    @Override
    Map getTaskVariables() {
        return taskVariables.asImmutable()
    }

    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }
}