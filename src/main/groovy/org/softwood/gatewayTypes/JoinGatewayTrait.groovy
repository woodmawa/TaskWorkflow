package org.softwood.gatewayTypes

trait JoinGatewayTrait extends GatewayTrait{

    //run relevant work closure for this task type
    public def join () {
        def result = gatewayResourceProcessor (taskWork)
        addToProcessRunTasks()
        result
    }

}