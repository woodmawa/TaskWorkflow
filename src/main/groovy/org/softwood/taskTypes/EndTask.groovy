package org.softwood.taskTypes

import groovy.transform.MapConstructor
import groovy.util.logging.Slf4j

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Slf4j
class EndTask implements TaskTrait {
    String taskType = this.class.getSimpleName()
    String taskCategory = "task"


    private Closure tidyUpProcess = {}

    private CompletableFuture  end () {
        startTime = LocalDateTime.now()
        status =TaskStatus.RUNNING

       //initiate any tidy up actions
       tidyUpProcess (taskVariables)

       endTime = LocalDateTime.now()
       status = TaskStatus.COMPLETED
       CompletableFuture.completedFuture("end task completed")
    }

    CompletableFuture execute() {
        end()
    }

     CompletableFuture execute(Map inputVariables) {
        taskVariables ? inputVariables: [:]
        end()  //todo
    }


    @Override
    String getTaskType() {
        return taskType
    }
}
