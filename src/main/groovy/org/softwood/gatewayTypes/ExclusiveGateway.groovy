package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus

import java.time.LocalDateTime

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ExclusiveGateway implements ConditionalGatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway
    Map<String, Closure> conditionsMap = [:]
    def conditionsEvaluationResults

    def evaluateConditions (def value ) {
        status = TaskStatus.COMPLETED
        startTime = LocalDateTime.now()
        List out = []
        int counter = 0
        conditionsMap.each { Map.Entry<String, Closure> entry ->
             out << [++counter, entry.getKey(), entry.getValue()?.call(value)]
        }
        endTime = LocalDateTime.now()
        this.status = TaskStatus.COMPLETED
        conditionsEvaluationResults = out

    }
}
