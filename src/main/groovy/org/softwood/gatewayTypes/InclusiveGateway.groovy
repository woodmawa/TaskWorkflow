package org.softwood.gatewayTypes

import java.util.concurrent.CompletableFuture

class InclusiveGateway implements GatewayTaskTrait{
    String taskType = this.class.getSimpleName()
    String taskCategory = "gateway"

    CompletableFuture previousTaskOutcome

}
