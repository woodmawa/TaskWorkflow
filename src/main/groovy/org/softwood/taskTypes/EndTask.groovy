package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class EndTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task

    EndTask () {
        taskWork = EndTask::end //link work to correct do work method
    }

    //any private tidy up actions for an end task can be run via this closure
    private Closure tidyUpProcess = {}

    private CompletableFuture  end (Map inputVariables =[:]) {
       //initiate any tidy up actions
        if (inputVariables)
            taskVariables = inputVariables
        log.info "endTask: tidying up current task branch "

        taskResult = CompletableFuture.completedFuture("end task '$taskName' completed")
    }


}
