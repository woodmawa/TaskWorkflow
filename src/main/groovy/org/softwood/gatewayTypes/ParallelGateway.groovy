package org.softwood.gatewayTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.taskTypes.TaskCategories

import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ParallelGateway implements GatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway


    ParallelGateway () {
        taskCategory = TaskCategories.Gateway
        taskType = this.class.getSimpleName()
    }

    private def run (Map value=[:]) {
        //tbc
        List out = []
        int counter = 0
        List<List> previous = previousTaskResults
        previous.each { List taskAndResult ->
            out << [++counter, taskAndResult[0], (CompletableFuture) taskAndResult[1]]
        }
        println "--> parallel gw : previousTaskResults " + out
        Optional.of (out)
    }

    def fork (Map value=[:]) {
        gatewayResourceProcessor (ParallelGateway::run, value)

    }

    List forkedTasks () {
        def forkedTasks = parentProcess.graph.getFromVertices(this.taskName)
        forkedTasks
    }
}
