package org.softwood.gatewayTypes

import org.softwood.taskTypes.TaskTrait

class ExclusiveGateway implements GatewayTaskTrait {
    String taskCategory = "gateway"


    @Override
    String getTaskType() {
        return null
    }
}
