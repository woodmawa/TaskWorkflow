package org.softwood.gatewayTypes

import groovy.util.logging.Slf4j
import org.softwood.taskTypes.TaskStatus

import java.util.concurrent.CompletableFuture

@Slf4j
trait JoinGatewayTrait extends GatewayTrait{

    //run relevant work closure for this task type
    public def join () {
        if (!this.isReadyToExecute()) {
            //if not ready to run yet just wait till called again
            log.info "join task $this.taskName, didnt have all its requiredPredecessors added yet, cycle round till called again "
            CompletableFuture result  = CompletableFuture.completedFuture("Not Ready to Run: not all inputs for the Join have been posted to the requiredPredecessors ")
            return result
        }

        def result = gatewayResourceProcessor (taskWork)
        if (status == TaskStatus.COMPLETED)
            addToProcessRunTasks()
        result
    }

}