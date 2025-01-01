package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories

import java.util.concurrent.CountDownLatch

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class JoinGateway implements GatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway
    CountDownLatch latch

    JoinGateway () {
        taskWork = JoinGateway::doJoin  //link work to correct do work method
    }

    private def doJoin () {
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
