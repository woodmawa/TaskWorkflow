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

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@ToString
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
    Map runTaskReferenceLookup = new ConcurrentHashMap()
    Map vertexTaskCache = new ConcurrentHashMap()

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

    //if task is already generated get it from
    Optional<Task> taskLookup (String name) {
        Task generatedTaskInstance  = runTaskReferenceLookup.get (name)
        Optional.ofNullable(generatedTaskInstance)
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

        ProcessHistory.closedProcesses << this

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

        // Start the 'start' vertex as root
        graph = fromTemplate.processDefinition
        Vertex head = graph.getHead()
        Map initialValues = null
        Optional<Task> startTask = TaskTypeLookup.getTaskFor(head, initialValues)
        CompletableFuture freshStart = CompletableFuture.completedFuture("process [${processId}] started".toString())
        //processVertex(head, null, freshStart)
        processTask (startTask.get())

        this

    }

    /**
     * helper function, looks in process instance cache first, and returns task if it exists, elses goes to
     * factory  TaskTypeLookup to generate the nex task, and sets refereence in the cache
     * @param vertex
     */
    private Task getTaskForVertex (Vertex vertex) {
        TaskTrait task
        if (vertexTaskCache.contains (vertex)) {
            task = vertexTaskCache.get(vertex)
        } else {
            Optional<Task> optionalTask = TaskTypeLookup.getTaskFor(vertex, [taskName: vertex.name])
            if (optionalTask.isEmpty()) {
                log.warn("Task for vertex [${vertex.name}] not found")
                return
            }

            task = optionalTask.get()
            task.parentProcess = this
            task.parentProcess.vertexTaskCache.putIfAbsent(vertex, task) //add task to cache for this process
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
        List<Optional<Task>> nextTasks = nextVertices.collect {getTaskForVertex(it)}
        if (currentTask.status == TaskStatus.NOT_REQUIRED) {
            //set not required on all its successors - assumes no crossed beams in the graph!
            nextTasks.each {it.get().status == TaskStatus.NOT_REQUIRED}
        }
        nextTasks.collect{it.get()}
    }

    /**
     * process current Task from graph tree in the process
     * @param task
     */
    private void processTask (TaskTrait task) {
        //add this process instance as parent for the task to be processed
        task.parentProcess = this
        if (!task.isReadyToExecute()) {
            //if not ready to run yet just wait till called again
            return
        }

        if (task.taskType == "StartTask") {
            //no pre existing future state so create one here
            CompletableFuture freshStart = CompletableFuture.completedFuture("""process [${processId}] started""".toString())
            task.setPreviousTaskResults (Optional.empty(), freshStart)
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

        //process the successor tasks
        List<Task> successors = getTaskSuccessors (task)
        successors.each { successor ->
            successor.setPreviousTaskResults(Optional.of(task), task.taskResult)
            // Update predecessor tracking for join nodes
            if (successor.taskType instanceof  JoinGateway && task.status != TaskStatus.NOT_REQUIRED) {
                //add to required tasks for the join
                task.requiredPredecessors << task
            }
            //do this in parallel ?  successors.collectParall {-> processTask (it)} ??
            processTask(successor)
        }
    }


    /**
     * when ready in processTask - then execute the task through this
     * @param task
     * @return
     */
    private def executeTask (Task task) {
        CompletableFuture currentTaskResult

        if (task.taskCategory == TaskCategories.Task) {
            ExecutableTaskTrait etask = task
            switch ( etask.taskType) {
                case "StartTask" :
                    //if first start task - then set previous task result with optional empty
                    etask.setPreviousTaskResults(Optional.empty(), previousResult)
                    currentTaskResult = etask.execute()
                    break
                case "EndTask" :
                    currentTaskResult = etask.execute()  // Execute and run tidy up

                    log.info("End of process [$processId] with variables " + processVariables.toString())
                    break
                case "ScriptTask" :
                    currentTaskResult = etask.execute()
                    break
                case "TerminateTask":
                    currentTaskResult = etask.execute()
                    break

                default :
                    log.info("Next task [$etask.taskName] is of category  [$etask.taskCategory]")
                    break
            }
        } else if (task.taskCategory == TaskCategories.Gateway) {
            switch (task.taskType) {
                case "ParallelGateway":
                    println "parallel: execute all the outgoing paths "
                    ParallelGateway forkTask = task
                    forkTask.fork()     //start all paths out from
                    List<Vertex> forkedTasks = forkTask.forkedTasks()
                    for (taskVertex in forkedTasks ){
                        //may work below and not be necessary
                    }

                    break
                case "ExclusiveGateway":
                    ConditionalGatewayTrait cgtask = task
                    cgtask.conditionsMap = vertex.conditionsMap
                    cgtask.setPreviousTaskResults(Optional.of(previousVertex), previousResult)
                    println "exclusive: evaluate conditions and pick single path to follow "
                    def condRes = cgtask.evaluateConditions('Will')
                    log.info "exclusive gateway : evaluated conditions result : " + condRes
                    break
                case "InclusiveGateway":
                    println "inclusive: pick all paths where condition check is true  "
                    ConditionalGatewayTrait cgtask = task
                    cgtask.conditionsMap = vertex.conditionsMap
                    cgtask.setPreviousTaskResults(Optional.of(previousVertex), previousResult)

                    break
                case "JoinGateway":
                    JoinGateway joinTask = task
                    //get expected number of inputs
                    List<Vertex> predecessors = graph.getFromVertices(vertex)
                    List<Task> predecessorTasks = predecessors.collect {TaskTypeLookup.getTaskFor(it)}
                    return predecessorTasks.collect {it.taskResult}

                    ///la la la
                    //int latchExpectedInputs = nextVertices?.size()
                    //joinTask.latch = new CountDownLatch(latchExpectedInputs)  //setup the expected numbers of join inputs
                    break

                default:
                    log.info("Next Gateway task [$task.taskName] is of category  [$task.taskCategory]")
                    /*return vertex.conditionsMap.every { name, condition ->
                        condition.call(vertex)
                    }*/
                    break
            }

        }
        //record that this task was in fact processed by the traversal process
        TaskHistory.closedTasks << [this.processId, task]
    }

    private void handleNextTasks(List<Task> tasks){
        log.info "processing successor tasks for  '${currentVertex.name}'"

        Task currentTask

        for (Task task : tasks) {

            processVertex(nextVertex, currentVertex, previousTaskResult)
        }
    }

    private void handleNextVertices(Vertex currentVertex, CompletableFuture previousTaskResult) {
        log.info "processing process graph vertex '${currentVertex.name}'"
        List<Vertex> nextVertices = graph.getToVertices(currentVertex.name)

        Task currentTask
        if (currentTask = TaskTypeLookup.getTaskFor(currentVertex)) {
            if (currentTask.status == TaskStatus.NOT_REQUIRED) {
                //set not not required on successors
            }
        }
        for (Vertex nextVertex : nextVertices) {

            processVertex(nextVertex, currentVertex, previousTaskResult)
        }
    }

    //reworked process sample
    private void processVertex(Vertex vertex, Vertex previousVertex, CompletableFuture previousResult) {
        TaskTrait task

        task = getTaskForVertex (vertex)

        if (!task.isReadyToExecute()) {
            return
        }

        //set this process as parent for the task
        task.setPreviousTaskResults(Optional.of(task), previousResult)

        this.executeTask(task)

        if (status != ProcessState.Completed){
            List<Vertex> nextVertices = graph.getToVertices(currentVertex.name)
            List<Task> nextTasks = nextVertices.collect {TaskTypeLookup.getTaskFor(it)}
            if (task.status == TaskStatus.NOT_REQUIRED) {
                nextTasks.each {it.status == TaskStatus.NOT_REQUIRED}
            }
            handleNextVertices (vertex, currentTaskResult)
            //handleNextTasks(nextTasks)
        }

        /*CompletableFuture result = task.execute()
        result.whenComplete { res, throwable ->
            if (throwable != null) {
                log.error("Task execution failed for vertex [${vertex.name}]", throwable)
            } else {
                log.info("Task completed for vertex [${vertex.name}] with result [$res]")
                handleNextVertices(task, vertex)
            }
        }*/

    }

}
