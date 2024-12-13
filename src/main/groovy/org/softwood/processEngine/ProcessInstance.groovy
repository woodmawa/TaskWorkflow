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
    TaskGraph graph

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
        /*graph = fromTemplate.processDefinition
        Vertex head = graph.getHead()
        List<Vertex> nextVertices = graph.getToVertices(head.name)
        Optional<Task> optionalStart = taskTypeLookup.getTaskFor(head, [taskName: head.name])
        Task previousTask = optionalStart.get()

        CompletableFuture result
        optionalStart.ifPresentOrElse({task -> result = task.execute() },
                                                    {result = new CompletableFuture().complete("task not found")})

        //walk the graph starting each task as required ...
        processNextVertices (previousTask, result,  nextVertices ) */

        // Start the 'start' vertex as root
        graph = fromTemplate.processDefinition
        Vertex head = graph.getHead()
        processVertex(head, CompletableFuture.completedFuture(Optional.empty()))


        this

    }

    //reworked process sample
    private void processVertex(Vertex vertex, CompletableFuture previousResult) {
        Optional<Task> optionalTask = taskTypeLookup.getTaskFor(vertex, [taskName: vertex.name])
        if (optionalTask.isEmpty()) {
            log.warn("Task for vertex [${vertex.name}] not found")
            return
        }

        Task task = optionalTask.get()
        task.setPreviousTaskResults(null, previousResult)  //may fail !

        CompletableFuture result = task.execute()
        result.whenComplete { res, throwable ->
            if (throwable != null) {
                log.error("Task execution failed for vertex [${vertex.name}]", throwable)
            } else {
                log.info("Task completed for vertex [${vertex.name}] with result [$res]")
                handleNextVertices(task, vertex)
            }
        }
    }

    private void handleNextVertices(Task task, Vertex vertex) {
        List<Vertex> nextVertices = graph.getToVertices(vertex.name)
        for (Vertex nextVertex : nextVertices) {
            switch (task.taskType) {
                case "EndTask":
                    task.execute()  // Execute and run tidy up
                    log.info("End of process [$processId] with variables " + processVariables.toString())
                    break
                case "ScriptTask":
                    processVertex(nextVertex, task.execute())
                    break
                default:
                    log.info("Next task is of type [$task.taskType]")
                    processVertex(nextVertex, task.execute())
                    break
            }
        }
    }

    /*** deprecate ? */
    private processNextVertices (Task previousTask, CompletableFuture result, List<Vertex> nextVertices) {
        Optional<Task> next

        for ( vertex in nextVertices) {
            next = taskTypeLookup.getTaskFor(vertex, [taskName:"${vertex.name}"])
            Task task = next.get()
            task.setPreviousTaskResults(previousTask, result)
            switch (task.taskType) {
                case "EndTask" -> {
                    task.execute()  //execute and run tidyUp()
                    log.info("end of process [$processId] with variables " + processVariables.toString())
                }
                case "ScriptTask" -> {
                    result = task.execute()
                    List<Vertex> vertexList    = graph.getToVertices(task.taskName)
                    //Optional<Task> optionalTask = taskTypeLookup.getTaskFor(vertexList[0], [taskName: vertexList[0].name])
                    processNextVertices (task, result, vertexList)
                }
                default -> {
                    println "next task was a $task.taskType"
                    result = task.execute()
                }
            }
        }

    }

}
