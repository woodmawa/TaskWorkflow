package org.softwood.gatewayTypes

import groovy.transform.ToString
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskTrait

import java.util.function.Predicate

@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ExclusiveGateway implements GatewayTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Gateway
    Map<String, Closure> conditions = [:]

    TaskTrait evaluateConditions (def value ) {
        List out = []
        int counter = 0
        conditions.each { Map.Entry<String, Closure> entry ->
            out << [++counter, entry.getKey(), entry.getValue().call(value)]
        }
        println "evals looked like : " + out

    }
}
