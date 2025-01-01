package org.softwood.taskTypes

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

interface Task<T, R> {
    void setTaskVariables (Map vars)
    Map<String, ? extends Object> getTaskVariables()
    Map<String, ? extends Object> getProcessVariables()
    void setTaskName(String name)
    UUID getTaskId ()
    String getTaskName ()
    String getTaskType()
    TaskCategories getTaskCategory()
    TaskStatus getStatus ()
    LocalDateTime getStartTime()
    LocalDateTime getEndTime()
    CompletableFuture getTaskResult()

    void setPreviousTaskResults (Optional<Task> currentTask, CompletableFuture previousTaskResult)
}