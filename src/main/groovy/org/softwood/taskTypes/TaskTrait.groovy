package org.softwood.taskTypes

import org.softwood.gatewayTypes.Gateway

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

trait TaskTrait implements  Task, Gateway {
    String taskName
    String taskType
    String taskCategory //task or gateway
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation = {var ->}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING

    //for first start task the previous task will be Optional.empty()
    List<List> previousTaskResults = []

    @Override
    void setPreviousTaskResults (Optional<Task> previousTask, CompletableFuture result) {
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

    //@Override
    String getTaskType () {
        taskType
    }

    //@Override
    String getTaskCategory () {
        taskCategory
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