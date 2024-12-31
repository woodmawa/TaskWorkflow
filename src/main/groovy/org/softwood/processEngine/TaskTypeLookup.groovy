package org.softwood.processEngine

import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper
import org.softwood.graph.*
import org.softwood.taskTypes.*
import org.softwood.gatewayTypes.*
import org.springframework.stereotype.Component

import javax.naming.Name

@Component ("taskTypeLookup")
@Slf4j
class TaskTypeLookup {
    static Map taskTypeLookup = [:]

    /**
     * links vertex type with Class to construct
     */
    static {
        taskTypeLookup.putIfAbsent( StartTask.class.simpleName,  StartTask )
        taskTypeLookup.putIfAbsent( EndTask.class.simpleName, EndTask )
        taskTypeLookup.putIfAbsent( TerminateTask.class.simpleName, TerminateTask )
        taskTypeLookup.putIfAbsent( ScriptTask.class.simpleName,  ScriptTask )
        taskTypeLookup.putIfAbsent( ClassDelegateTask.class.simpleName, ClassDelegateTask)

        //gateways
        taskTypeLookup.putIfAbsent( ExclusiveGateway.class.simpleName, ExclusiveGateway )
        taskTypeLookup.putIfAbsent( InclusiveGateway.class.simpleName, InclusiveGateway )
        taskTypeLookup.putIfAbsent( ParallelGateway.class.simpleName, ParallelGateway )

    }


    static Optional<Task> lookup (Vertex vertex) {

        def taskType = taskTypeLookup[vertex.type.simpleName]
        Optional.ofNullable (taskType)
    }

    static Optional<Task> getTaskFor (Vertex vertex, Map initValues=[:]) {
        Optional optionalTask

        if (vertex == null)
            optionalTask = Optional.empty()

        Class taskType = taskTypeLookup[vertex.type.simpleName]
        if (taskType) {
            def object = taskType.getDeclaredConstructor().newInstance()
            if (object){
                initValues.each {k, v ->
                    object["$k"] = v
                }
            }
            object["taskName"] = vertex.name
            object["taskId"] = UUID.randomUUID() //assign unique task ref
            //object["taskType"] = vertex.type
            optionalTask = Optional.ofNullable(object)
        }
        else
            optionalTask = Optional.empty()
        optionalTask

    }
}
