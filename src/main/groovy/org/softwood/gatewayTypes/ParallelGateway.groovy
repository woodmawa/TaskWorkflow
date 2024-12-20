package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories

import java.util.concurrent.CompletableFuture

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ParallelGateway implements GatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway


    private def run () {
        //tbc
        List out = []
        int counter = 0
        List previous = previousTaskResults
        previous.each { Map.Entry<String, Closure> entry ->
            out << [++counter, entry.getKey(), entry.getValue()]
        }
        out
    }

    def evaluateConditions (value ) {
        gatewayResourceProcessor (JoinGateway::run)

    }

}
