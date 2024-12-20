package org.softwood.taskTypes

import org.softwood.gatewayTypes.Gateway
import org.softwood.processEngine.ProcessInstance

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

trait TaskTrait implements  Task {
    String taskName
    abstract String taskType  //simple name for implementing task class
    abstract TaskCategories taskCategory //task or gateway
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation = {var ->}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.PENDING
    ProcessInstance parentProcess
    CompletableFuture taskResult


    //for first start task the previous task will be Optional.empty()
    List<List> previousTaskResults = []


    @Override
    void setPreviousTaskResults (Optional<Task> previousTask, CompletableFuture result) {
        previousTaskResults << [previousTask,result]
    }

    //@Override
    void setTaskName (String name) {
        this.taskName = name
    }

    //@Override
    String getTaskName () {
        taskName
    }

    //@Override
    //get from implementing class
    String getTaskType () {
        taskType
    }

    //@Override
    //get from implementing class
    TaskCategories getTaskCategory () {
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

    /**
     * sets up state for running task
     * @param state
     * @param taskVariables
     * @return
     */
    def setupTask (TaskStatus state, Map taskVariables=[:]) {
        this.taskVariables = taskVariables
        startTime = LocalDateTime.now()
        status = state

    }

    /**
     * closes down state for running task
     * @param state
     * @return
     */
    CompletableFuture closeOutTask (TaskStatus state) {
        endTime = LocalDateTime.now()
        status = state
        taskResult
    }

    /**
     * surround method - that setsup the task state, runs the task and exits and tides up
     * @param action
     * @param inputVariables
     * @return
     */
    def taskResourceProcessor (Closure action, Map inputVariables = null) {
        try {
            setupTask(TaskStatus.RUNNING)
            action?.call (this, inputVariables)
            closeOutTask (TaskStatus.COMPLETED)
        } catch (Exception exception) {
            closeOutTask(TaskStatus.EXCEPTION)
        }
    }
}