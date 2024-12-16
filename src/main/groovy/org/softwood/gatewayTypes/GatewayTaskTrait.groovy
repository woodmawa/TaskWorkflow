package org.softwood.gatewayTypes

import org.softwood.taskTypes.TaskTrait

trait GatewayTaskTrait extends TaskTrait {
    abstract String taskCategory
    abstract String taskType
}
