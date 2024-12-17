package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ExclusiveGateway implements ConditionalGatewayTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway
    Map<String, Closure> conditionsMap = [:]
    def conditionsEvaluationResults

    def evaluateConditions (def value ) {
        List out = []
        int counter = 0
        conditionsMap.each { Map.Entry<String, Closure> entry ->
             out << [++counter, entry.getKey(), entry.getValue()?.call(value)]
        }
        conditionsEvaluationResults = out
    }
}
