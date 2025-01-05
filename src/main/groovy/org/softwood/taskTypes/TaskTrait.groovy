package org.softwood.taskTypes

import groovy.util.logging.Slf4j
import org.softwood.graph.Vertex
import org.softwood.processEngine.ProcessInstance

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

@Slf4j
trait TaskTrait  implements  Task  {
    UUID taskId
    String taskName
    abstract String taskType  //simple name for implementing task class
    abstract TaskCategories taskCategory //task or gateway
    Map<String, ? extends Object> taskVariables = [:]
    Closure taskInitialisation = {var ->}
    LocalDateTime startTime, endTime
    TaskStatus status = TaskStatus.NOT_STARTED
    ProcessInstance parentProcess
    CompletableFuture taskResult
    Closure taskWork = {/* no op*/}
    //for first start task the previous task will be Optional.empty()
    List<List> previousTaskResults = []
    Map initialValues = new ConcurrentHashMap()
    //required to track the join task ready to run calculation
    List<Task> requiredPredecessors = []  //todo needs to be concurrent something ...

    /**
     * determines if all the predecessor tasks for a join have completed
     * @return
     */
    boolean isReadyToExecute() {
        // Special logic for join nodes, check all predessors have completed
        if (getTaskType() == "JoinGateway") {
            //are all predecessors running or not required ?

            List<TaskTrait> requiredTasks //todo complete here
            return requiredPredecessors.every { Task predecessor ->
                //def predecessor = parentInstance.taskCache
                //graph.lookupVertexByTaskName(predecessorName)
                def isReady = (predecessor?.status != TaskStatus.NOT_STARTED ||
                        predecessor?.status != TaskStatus.NOT_REQUIRED )
                isReady
            }
        }
        // Regular nodes just need to be NOT_STARTED
        return status == TaskStatus.NOT_STARTED
    }

    void setInitialValues (Map initialTaskValues=[:]) {
        initialValues.putAll(initialTaskValues)
    }

    ProcessInstance getParentInstance () {
        parentProcess
    }

    /**
     * add previous task, expected type and Future for the task result
     * @param previousTask
     * @param expectedFutureType
     * @param result
     */
    void setPreviousTaskResults (Optional<Task> previousTask, CompletableFuture result) {
        previousTaskResults << [previousTask, result]
    }

    List<List> getPreviousTaskResults () {
        previousTaskResults.asImmutable()
    }

    void setTaskName (String name) {
        this.taskName = name
    }

    String getTaskName () {
        taskName
    }

    //get from implementing class
    String getTaskType () {
        taskType
    }

    //get from implementing class
    TaskCategories getTaskCategory () {
        taskCategory
    }

    void setTaskVariables(Map vars) {
        taskVariables = vars?:[:]
    }

    Map getTaskVariables() {
        return taskVariables.asImmutable()
    }

    void setProcessVariables(Map vars) {
        this.parentProcess.processVariables.putAll(vars?:[:])
    }

    Map getProcessVariables() {
        return this.parentProcess.processVariables
    }

    void addToProcessRunTasks () {
        def process = this.parentProcess
        process.addTaskToProcessRunTasks (this)
    }


    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
    }

    /**
     * helper routine tidy, can be overriden in implementing class to tidy up an
     * resources used by the task
     */
    def tidyUpTaskResources () {
        log.debug "tidy up any task resources called "
    }

    /**
     * sets up state for running task
     * @param state
     * @param taskVariables
     * @return
     */
    def setupTask (TaskStatus state, Map taskVariables=[:]) {
        this.taskVariables = taskVariables
        startTime = LocalDateTime.now()
        status = state

    }

    /**
     * closes down state for running task
     * @param state
     * @return
     */
    CompletableFuture closeOutTask (TaskStatus state) {
        endTime = LocalDateTime.now()
        status = state
        tidyUpTaskResources()

        taskResult     //just return the taskResult future
    }

}