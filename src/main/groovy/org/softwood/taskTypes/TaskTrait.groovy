package org.softwood.taskTypes

import groovy.util.logging.Slf4j
import org.softwood.gatewayTypes.Gateway
import org.softwood.processEngine.ProcessInstance

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Slf4j
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


    ProcessInstance getParentInstance () {
        parentProcess
    }

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
        taskResult     //just return the taskResult future
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
            log.info "TaskTrait: task resource processor , task: $taskName, action.doCall ($this, $inputVariables)"
            def result = action?.call (this, inputVariables)
            return closeOutTask (TaskStatus.COMPLETED)
        } catch (Exception exception) {
            log.info "TaskTrait: task resource, action.doCall threw exception $exception with delegate set as $action.delegate"
            return closeOutTask(TaskStatus.EXCEPTION)
        }
    }

    def gatewayResourceProcessor (Closure action, Map inputVariables = null) {
        try {
            setupTask(TaskStatus.RUNNING)
            action?.call (this, inputVariables)
            closeOutTask (TaskStatus.COMPLETED)
        } catch (Exception exception) {
            log.info "TaskTrait: gateway resource, action.doCall threw exception $exception with delegate set as $action.delegate"
            closeOutTask(TaskStatus.EXCEPTION, exception)
        }
    }
}