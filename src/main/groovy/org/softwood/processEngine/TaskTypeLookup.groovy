package org.softwood.processEngine

import org.codehaus.groovy.runtime.InvokerHelper
import org.softwood.graph.*
import org.softwood.taskTypes.*
import org.softwood.gatewayTypes.*
import org.springframework.stereotype.Component

@Component
class TaskTypeLookup {
    Map taskTypeLookup = [:]

    TaskTypeLookup () {
        taskTypeLookup.putIfAbsent( "start",  StartTask )
        taskTypeLookup.putIfAbsent( "end", EndTask )
        taskTypeLookup.putIfAbsent( "script",  ScriptTask )
        taskTypeLookup.putIfAbsent( "classDelegate", ClassDelegateTask)

        //gateways
        taskTypeLookup.putIfAbsent( "exclusive}", ExclusiveGateway )
        taskTypeLookup.putIfAbsent( "inclusive", InclusiveGateway )
        taskTypeLookup.putIfAbsent( "parallel", ParallelGateway )

    }


    Optional<Task> lookup (Vertex vertex) {

        def taskType = taskTypeLookup[vertex.name.toLowerCase()]
        Optional.ofNullable (taskType)
    }

    Optional<Task> getTaskFor (Vertex vertex, Map initValues=[:]) {
        Optional optionalTask

        Class taskType = taskTypeLookup[vertex.name.toLowerCase()]
        if (taskType) {
            def object = taskType.getDeclaredConstructor().newInstance()
            if (object){
                initValues.each {k, v ->
                    object["$k"] = v
                }
            }
            optionalTask = Optional.ofNullable(object)
        }
        else
            optionalTask = Optional.empty()
        optionalTask

    }
}
