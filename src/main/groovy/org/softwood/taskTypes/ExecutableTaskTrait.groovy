package org.softwood.taskTypes

import groovy.util.logging.Slf4j

import java.util.concurrent.CompletableFuture

@Slf4j
trait ExecutableTaskTrait<R> extends TaskTrait {
    /**
     * surround method - that setsup the task state, runs the task and exits and tides up
     * @param action
     * @param inputVariables
     * @return
     */
    private def taskResourceProcessor (Closure action, Map inputVariables = null) {
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

    public CompletableFuture execute() {
        taskResourceProcessor (this.taskWork)
    }

    public CompletableFuture execute(Map inputVariables) {
        taskResourceProcessor (this.taskWork, inputVariables)
    }

}