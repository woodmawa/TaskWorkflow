package org.softwood.gatewayTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.taskTypes.TaskCategories

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class JoinGateway implements JoinGatewayTrait  {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway

    JoinGateway () {
        taskWork = JoinGateway::doJoin  //link work to correct do work method
    }

    private def doJoin (Map variables = [:]) {
        //should only have got here if all the requiredPredecessors had been posted
        List<CompletableFuture> predecessorTaskFutures = requiredPredecessors.collect{it.taskResult}

        //wait for all the futures to complete
        CompletableFuture<Void> allDone
        if (predecessorTaskFutures) {
            CompletableFuture.allOf(*predecessorTaskFutures).join()
        }

        def combinedTaskResults = predecessorTaskFutures.collect {it.get()}
        //tbc
        List out = []
        int counter = 0
        List<List> previousResults = previousTaskResults
        previousResults.each { List<Optional,CompletableFuture> entry ->
            out << [++counter, entry[0]?.get(), entry[1]?.get()]
        }
        //taskResult future is the merge of the various task, and there completed results
        taskResult = CompletableFuture.completedFuture (out)
    }


}
