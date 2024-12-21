package org.softwood.taskTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.processEngine.ProcessInstance

import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class TerminateTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    private CompletableFuture  terminate (Map variables = [:]) {
        taskVariables = variables ?: taskVariables
        if (taskInitialisation)
            taskInitialisation (taskVariables)
        parentInstance.tidyUpProcessAndExit()
        log.info "TerminateTask: terminated process ${parentInstance.getProcessId()}, and exit "
        taskResult = CompletableFuture.completedFuture("terminate complete process task '$taskName' completed")
    }

    @Override
    CompletableFuture execute() {
        taskResourceProcessor (TerminateTask::terminate )
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        taskResourceProcessor (TerminateTask::terminate, inputVariables)
    }

    @Override
    String getTaskType() {
        taskType
    }
}
