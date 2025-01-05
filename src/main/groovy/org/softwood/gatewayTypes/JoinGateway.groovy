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
    CountDownLatch latch

    JoinGateway () {
        taskWork = JoinGateway::doJoin  //link work to correct do work method
    }

    private def doJoin (Map variables = [:]) {
        List<CompletableFuture> futureResults = requiredPredecessors.collect{it.taskResult}
        //wait for all the futures to complete
        if (futureResults) {
            CompletableFuture<Void> allDone = CompletableFuture.allOf(*futureResults).join()
            allDone.whenComplete { log.info "all gateway predecessors now complete " }
        }
        //tbc
        List out = []
        int counter = 0
        List previous = previousTaskResults
        previous.each { Map.Entry<String, Closure> entry ->
            out << [++counter, entry.getKey(), entry.getValue()?.call()]
        }
        out
    }


}
