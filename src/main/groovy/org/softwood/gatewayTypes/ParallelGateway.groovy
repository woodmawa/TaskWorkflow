package org.softwood.gatewayTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.graph.Vertex
import org.softwood.taskTypes.Task
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
        taskWork = ParallelGateway::doFork
    }

    private def doFork (Map value=[:]) {
        //tbc
        List out = []
        int counter = 0
        List<List> previous = previousTaskResults
        previous.each { List taskAndResult ->
            Optional<Task> opTask = taskAndResult[0]
            out << [++counter, opTask.get(), (CompletableFuture) taskAndResult[1]]
        }
        println "--> parallel gw : previousTaskResults " + out

        //only expecting one previous task going into a parallel gateway
        //so relay the parallel gateways previousTaskResult future to be that for the parallel node
        taskResult = previousTaskResults[0][1]  //relay the input
    }

    public def fork (Map value=[:]) {
        def result = gatewayResourceProcessor (taskWork, value)
        addToProcessRunTasks()
        result
    }

    List<Vertex> forkedTasks () {
        def forkedTasks = parentProcess.graph.getFromVertices(this.taskName)
        forkedTasks
    }
}
