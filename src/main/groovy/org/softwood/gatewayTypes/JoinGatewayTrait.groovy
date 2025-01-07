package org.softwood.gatewayTypes

import groovy.util.logging.Slf4j
import org.softwood.taskTypes.TaskStatus

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Slf4j
trait JoinGatewayTrait extends GatewayTrait{

    //make this visible across all threads
    volatile CountDownLatch latch

    //run relevant work closure for this task type
    public def join () {

        //await time out returns false if call times out
        Boolean notTimedOut =latch.await(5L, TimeUnit.SECONDS)

        if (!notTimedOut == true) {
            //if not ready to run yet just wait till called again
            log.info "join task $this.taskName, didnt have all its requiredPredecessors set after 5 seconds,  latch.wait() timed out  "
            CompletableFuture result  = CompletableFuture.completedFuture("Not Ready to Run: not all inputs for the Join have been posted to the requiredPredecessors, latch wait timed out ")
            return result
        }

        //run the join action for the JoinGateway node
        def result = gatewayResourceProcessor (taskWork)
        if (status == TaskStatus.COMPLETED)
            addToProcessRunTasks()
        result
    }

}