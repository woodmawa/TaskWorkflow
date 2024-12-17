package org.softwood.gatewayTypes

import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskTrait

trait GatewayTrait extends TaskTrait {
    abstract TaskCategories taskCategory
    abstract String taskType


}
