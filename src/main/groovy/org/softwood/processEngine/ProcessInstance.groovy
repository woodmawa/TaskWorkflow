package org.softwood.processEngine


import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.gatewayTypes.GatewayTaskTrait
import org.softwood.graph.TaskGraph
import org.softwood.graph.Vertex
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.taskTypes.ExecutableTaskTrait
import org.softwood.taskTypes.Task
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskTrait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util.concurrent.CompletableFuture

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

    List<Task> taskHistory = []


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
        processVertex(head, null, CompletableFuture.completedFuture("process [${processId}] started".toString()))


        this

    }

    //reworked process sample
    private void processVertex(Vertex vertex, Vertex previousVertex, CompletableFuture previousResult) {
        CompletableFuture currentTaskResult
        Optional<Task> optionalTask = taskTypeLookup.getTaskFor(vertex, [taskName: vertex.name])
        if (optionalTask.isEmpty()) {
            log.warn("Task for vertex [${vertex.name}] not found")
            return
        }

        TaskTrait task = optionalTask.get()
        if (task.taskCategory == TaskCategories.Task) {
            ExecutableTaskTrait etask = task
            switch ( etask.taskType) {
                case "StartTask" :
                    etask.setPreviousTaskResults(Optional.ofNullable(previousVertex), previousResult)
                    currentTaskResult = etask.execute()
                    break
                case "EndTask" :
                    etask.setPreviousTaskResults(Optional.of(previousVertex), previousResult)
                    currentTaskResult = etask.execute()  // Execute and run tidy up

                    log.info("End of process [$processId] with variables " + processVariables.toString())
                    break
                case "ScriptTask" :
                    etask.setPreviousTaskResults(Optional.of(previousVertex), previousResult)
                    currentTaskResult = etask.execute()
                    break

                default :
                    log.info("Next task [$etask.taskName] is of category  [$etask.taskCategory]")
                    break
            }
            taskHistory << etask
        } else if (task.taskCategory == TaskCategories.Gateway) {
            GatewayTaskTrait gtask = task
            switch (gtask.taskType) {
                case "ParallelGateway":
                    println "parallel: execute all the outgoing paths "
                    break
                case "ExclusiveGateway":
                    gtask.setPreviousTaskResults(Optional.of(previousVertex), previousResult)
                    println "exclusive: evaluate conditions and pick single path to follow "
                    gtask.evaluateConditions('Will')
                    break
                case "InclusiveGateway":
                    println "inclusive: pick all paths where condition check is true  "
                    break
                default:
                    log.info("Next task [$gtask.taskName] is of category  [$gtask.taskCategory]")
                    break
            }
            taskHistory << gtask

        }

        handleNextVertices (vertex, currentTaskResult)
        /*
        result.whenComplete { res, throwable ->
            if (throwable != null) {
                log.error("Task execution failed for vertex [${vertex.name}]", throwable)
            } else {
                log.info("Task completed for vertex [${vertex.name}] with result [$res]")
                handleNextVertices(task, vertex)
            }
        }*/
    }

    //optimise with tampoline
    private void handleNextVertices(Vertex currentVertex, CompletableFuture previousTaskResult) {
        List<Vertex> nextVertices = graph.getToVertices(currentVertex.name)
        for (Vertex nextVertex : nextVertices) {
            processVertex(nextVertex, currentVertex, previousTaskResult)
        }
    }

}
