package org.softwood.gatewayTypes

import java.util.concurrent.CompletableFuture

class InclusiveGateway {
    String taskNature = "gateway"

    CompletableFuture previousTaskOutcome

}
