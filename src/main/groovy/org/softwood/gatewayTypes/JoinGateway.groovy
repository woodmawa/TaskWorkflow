package org.softwood.gatewayTypes

class JoinGateway implements GatewayTaskTrait {
    String taskType = this.class.getSimpleName()
    String taskCategory = "gateway"

}
