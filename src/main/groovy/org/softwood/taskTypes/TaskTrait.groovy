package org.softwood.taskTypes

import groovy.util.logging.Slf4j
import org.softwood.graph.Vertex
import org.softwood.processEngine.ProcessInstance

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
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

    //required to track the join task ready to run calculation
    Set<Task> requiredPredecessors = new ConcurrentSkipListSet()

    /**
     * determines if all the predecessor tasks for a join have completed
     * @return
     */
    boolean isReadyToExecute() {
        // Special logic for join nodes, check all predessors have completed
        if (taskType == "JoinTask") {
            return requiredPredecessors.every { predecessorName ->
                def predecessor = parentInstance.taskCache
                //graph.lookupVertexByTaskName(predecessorName)
                predecessor?.status == TaskStatus.COMPLETED
            }
        }
        // Regular nodes just need to be NOT_STARTED
        return status == TaskStatus.NOT_STARTED
    }

    //for first start task the previous task will be Optional.empty()
    List<List> previousTaskResults = []


    ProcessInstance getParentInstance () {
        parentProcess
    }

    @Override
    void setPreviousTaskResults (Optional<Task> previousTask, CompletableFuture result) {
        previousTaskResults << [previousTask,result]
    }

    //@Override
    void setTaskName (String name) {
        this.taskName = name
    }

    //@Override
    String getTaskName () {
        taskName
    }

    //@Override
    //get from implementing class
    String getTaskType () {
        taskType
    }

    //@Override
    //get from implementing class
    TaskCategories getTaskCategory () {
        taskCategory
    }

    @Override
    void setTaskVariables(Map vars) {
        taskVariables = vars?:[:]
    }

    @Override
    Map getTaskVariables() {
        return taskVariables.asImmutable()
    }

    void setProcessVariables(Map vars) {
        this.parentInstance.processVariables = vars?:[:]
    }

    Map getProcessVariables() {
        return this.parentInstance.processVariables.asImmutable()
    }

    void addTaskToRunTasksLookup () {
        def process = this.parentInstance
        process.addTaskToProcessRunTasks (this)
    }


    String executionDuration () {
        Duration duration = Duration.between(startTime, endTime)
        String formattedDuration =String.format("task execution time: %d ms", duration.toMillis())
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
        taskResult     //just return the taskResult future
    }

}