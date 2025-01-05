package org.softwood.gatewayTypes

import groovy.util.logging.Slf4j
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus
import org.softwood.taskTypes.TaskTrait

@Slf4j
trait GatewayTrait extends TaskTrait {
    abstract TaskCategories taskCategory
    abstract String taskType

    /**
     * surround processor to setup, run task and tidy up
     *
     * @param action
     * @param inputVariables
     * @return
     */
    def gatewayResourceProcessor (Closure action, Map inputVariables = null) {
        try {
            setupTask(TaskStatus.RUNNING)
            log.info "TaskTrait: task resource processor , task: $taskName, action.doCall ($this, $inputVariables)"
            action?.call (this, inputVariables)
            closeOutTask (TaskStatus.COMPLETED)
        } catch (Exception exception) {
            log.info "TaskTrait: gateway resource, action.doCall threw exception $exception with delegate set as $action.delegate"
            closeOutTask(TaskStatus.EXCEPTION)
        }
    }

}
