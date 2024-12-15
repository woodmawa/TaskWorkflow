package org.softwood.gatewayTypes

import java.util.concurrent.CompletableFuture

class ParallelGateway {
    String taskNature = "gateway"

    CompletableFuture previousTaskOutcome

}
