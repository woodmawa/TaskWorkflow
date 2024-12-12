package org.softwood.processEngine

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.NotNull
import org.softwood.graph.TaskGraph
import org.softwood.graph.Vertex
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.taskTypes.Task
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@ToString
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ProcessInstance {

    @Autowired
    private TaskTypeLookup taskTypeLookup

    private enum ProcessState {
        Pending,
        Running,
        Suspended,
        Completed
    }
    UUID processId
    ProcessTemplate fromTemplate
    ProcessState status
    Map processVariables

    ProcessInstance () {
        processVariables = [:]
        status = ProcessState.Pending
        processId = UUID.randomUUID()
    }

    void setProcessTemplate (ProcessTemplate template) {
        fromTemplate = template
    }

    ProcessInstance  start (Map processVariables=[:]) {
        log.info ("process [$processId] started from template ${fromTemplate.toString()}" )
        this.processVariables = processVariables
        status = ProcessState.Running

        //and now start the 'start' vertex as root
        TaskGraph graph = fromTemplate.processDefinition
        Vertex head = graph.getHead()
        List<Vertex> nextVertices = graph.getToVertices(head.name)
        Optional<Task> optionalStart = taskTypeLookup.getTaskFor(head, [taskName: "start"])

        CompletableFuture future
        optionalStart.ifPresentOrElse({ future = it.execute() },
                                                    {future = new CompletableFuture().complete("task not found")})

        //walk the graph starting each task as required ...
        if (nextVertices.size() == 1) {
            Optional<Task> next = taskTypeLookup.getTaskFor(nextVertices[0], [taskName:"end"])
            Task t = next.get()
            if (t.taskType == "EndTask") {
                log.info("end of process [$processId] with variables " + processVariables.toString())
            }
        } else {
            for ( vertex in nextVertices) {
                Optional<Task> next = taskTypeLookup.getTaskFor(vertex, [taskName:"??"])
            }
            //todo check and evaluate gateway tasks
        }
        this

    }



}
