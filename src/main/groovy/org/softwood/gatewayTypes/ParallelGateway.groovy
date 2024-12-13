package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories

import java.util.concurrent.CompletableFuture

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ParallelGateway implements GatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway

    CompletableFuture previousTaskOutcome

}
