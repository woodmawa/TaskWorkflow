package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ExclusiveGateway implements GatewayTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway


}
