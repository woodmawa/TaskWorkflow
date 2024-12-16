package org.softwood.gatewayTypes

import org.softwood.taskTypes.TaskTrait

class ExclusiveGateway implements GatewayTaskTrait {
    String taskType = this.class.getSimpleName()
    String taskCategory = "gateway"


}
