package org.softwood.taskTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.processEngine.ProcessInstance

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class StartTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    StartTask () {
        taskWork = StartTask::start //link work to correct do work method
    }

    private CompletableFuture  start (Map variables = [:]) {
        //no pre existing future state so create one here
        ProcessInstance pi = this.parentProcess
        CompletableFuture freshStart = CompletableFuture.completedFuture("""process [${pi.processId}] started""".toString())
        setPreviousTaskResults (Optional.empty(), freshStart)

        taskVariables = variables ?: taskVariables
        if (taskInitialisation)
            taskInitialisation (taskVariables)
        taskResult = CompletableFuture.completedFuture("start task '$taskName' completed")
    }


}
