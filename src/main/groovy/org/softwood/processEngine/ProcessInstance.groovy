package org.softwood.processEngine

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.softwood.gatewayTypes.ConditionalGatewayTrait
import org.softwood.gatewayTypes.JoinGateway
import org.softwood.gatewayTypes.JoinGatewayTrait
import org.softwood.gatewayTypes.ParallelGateway
import org.softwood.graph.TaskGraph
import org.softwood.graph.Vertex
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.taskTypes.ExecutableTaskTrait
import org.softwood.taskTypes.Task
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus
import org.softwood.taskTypes.TaskTrait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

import static java.util.stream.Collectors.*

@ToString (includeNames =true, includes = ["processId", "startTime", "endTime"])
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

class ProcessInstance {

    @Autowired
    private TaskTypeLookup taskTypeLookup

    private enum ProcessState {
        Not_Started,
        Pending,
        Running,
        Suspended,
        Closing,
        Completed
    }
    UUID processId
    ProcessTemplate fromTemplate
    ProcessState status
    Map processVariables
    TaskGraph graph
    LocalDateTime startTime, endTime
    Closure lastGasp = {}
    Map<String, Task> runTaskReferenceLookup = new ConcurrentHashMap<>()
    Map<String, Task> vertexTaskCache = new ConcurrentHashMap<>()

    ProcessInstance () {
        processVariables = [:]
        status = ProcessState.Not_Started
        processId = UUID.randomUUID()
    }

    void setProcessTemplate (ProcessTemplate template) {
        fromTemplate = template
    }

    void addTaskToProcessRunTasks (Task task) {
        runTaskReferenceLookup.putIfAbsent(task.taskName, task)   //once generated add task ref  for this process
    }

    void addTaskToCache (Task task) {
        vertexTaskCache.putIfAbsent(task.taskName, task)   //add task to cache
    }


    /**
     * at end of processing - close out and save to history
     * @return
     */
    ProcessInstance tidyUpProcessAndExit () {
        status = ProcessState.Closing
        endTime = LocalDateTime.now()
        if (lastGasp)
            lastGasp()

        ProcessHistory.addCompletedProcess(this)

        status = ProcessState.Completed
        this
    }

    /**
     * execute this process, and run the tasks from the graph
     * @param processVariables
     * @return
     */
    ProcessInstance startProcess(Map processVariables=[:]) {
        log.info ("process [$processId] started from template ${fromTemplate.toString()}" )
        this.processVariables = processVariables
        startTime = LocalDateTime.now()
        status = ProcessState.Running
        ProcessInstance pi = this

        // Start the 'start' vertex as root
        graph = fromTemplate.processDefinition
        Vertex head = graph.getHead()
        Map initialValues = [:]
        Task startTask = getTaskForVertex(head)

        processTask (startTask)


        //close out the process, and write to history
        tidyUpProcessAndExit()

        pi

    }

    /**
     * helper function, looks in process instance cache first, and returns task if it exists, elses goes to
     * factory  TaskTypeLookup to generate the nex task, and sets refereence in the cache
     *
     * note whilst 2 vertex with same name are 'equal' the is test is not true
     * try to ensure that for a process there is only one task in the taskCache using
     * vertex.name as the key
     * @param vertex
     */
    private Task getTaskForVertex (Vertex vertex, Map initialValues=[:]) {
        TaskTrait task
        if (vertexTaskCache.containsKey (vertex.toString())) {
            task = vertexTaskCache.get(vertex.toString())
        } else {
            Optional<Task> optionalTask = TaskTypeLookup.getTaskFor(vertex, [taskName: vertex.name, parentProcess:this])
            if (optionalTask.isEmpty()) {
                log.warn("Task for vertex [${vertex.name}] not found")
                return
            }

            task = optionalTask.get()
            task.setInitialValues ( initialValues?: [:])

            if (task.taskType == "JoinGateway") {
                JoinGateway gwTask = task
                if (gwTask.latch == null ) {
                    def expectedInputsList = getActionableTaskPredecessors (gwTask)
                    int expectedNumberInputs = expectedInputsList?.size()
                    gwTask.latch = new CountDownLatch(expectedNumberInputs)
                }

            }
            task.parentProcess.addTaskToCache(task) //add task to cache for this process
        }
        task
    }

    /**
     * helper function - get tasks successors by interpreting the graph template
     * @param currentTask
     * @return
     */
    private List<Task> getTaskSuccessors (Task currentTask) {

        List<Vertex> nextVertices = graph.getToVertices(currentTask.taskName)
        List<TaskTrait> allSuccessors = nextVertices.collect {getTaskForVertex(it)}
        if (currentTask.status == TaskStatus.NOT_REQUIRED) {
            //set not required on all its successors - assumes no crossed beams in the graph!
            allSuccessors.each {if (it.taskType != "JoinGateway") it.status = TaskStatus.NOT_REQUIRED }
        } else {
            List<TaskTrait> requiredSuccessors = allSuccessors.findAll {it.status != TaskStatus.NOT_REQUIRED }

            //take the current tasks future result and add that to each successor
            requiredSuccessors.each { stask ->
                //take current tasks future result, and add that to each successor
                stask.setPreviousTaskResults(Optional.of(currentTask), currentTask.taskResult)
                // Update predecessor tracking for join nodes
                if (stask.taskType == "JoinGateway" && currentTask.status != TaskStatus.NOT_REQUIRED) {
                    //add to required tasks for the join
                    JoinGateway gwtask = stask
                    gwtask.setRequiredPredecessors(currentTask)
                    gwtask.latch?.countDown()
                }
                stask

            }
            requiredSuccessors
        }
    }

    /**
     * get list of predecessor actioned tasks
     * @param currentTask
     * @return
     */
    private List<Task> getActionableTaskPredecessors (Task currentTask) {

        List<Vertex> predecessorVertices = graph.getFromVertices(currentTask.taskName)
        List<TaskTrait> predecessors = predecessorVertices.collect {getTaskForVertex(it)}

        List<TaskTrait> requiredPredecessors = predecessors.findAll {it.status != TaskStatus.NOT_REQUIRED }
        requiredPredecessors
    }

    /**
     * process current Task from graph tree in the process
     * @param task
     */
    private void processTask (TaskTrait task) {
       if (!task.isReadyToExecute()) {
            //if not ready to run yet just wait till called again
            return
        }

       //do the executable bit of processing
        def result
        if (task.taskCategory == TaskCategories.Task ){
            ExecutableTaskTrait etask = task
            result = etask.execute()

        } else if (task.taskCategory == TaskCategories.Gateway ) {
            switch (task.taskType) {
                case "JoinGateway":
                    JoinGatewayTrait jg = task
                    result = jg.join()
                    break
                case "ParallelGateway":
                    ParallelGateway  pg = task
                    result = pg.fork()
                    break
                default:
                    //conditional gateways here
                    break
            }
        }

        if (task.status == TaskStatus.COMPLETED) {
            //TaskHistory.addCompletedTask([this.processId, task])


            //process the successor tasks
            List<Task> successors = getTaskSuccessors(task)
            /*List<CompletableFuture> futureResults = successors.parallelStream()
                .map {Task successor -> processTask(successor)}
                .collect(toList())*/

            //sequential process the list for now
            successors.each { successor ->
                processTask(successor)
            }
        }
    }

    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }

}
