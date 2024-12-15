package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

interface Task<T, R> {
    void setTaskVariables (Map vars)
    Map<String, ? extends Object> getTaskVariables()
    void setTaskName(String name)
    String getTaskName ()
    String getTaskType()
    String getTaskCategory()
    void setPreviousTaskResults (Optional<Task> currentTask, CompletableFuture previousTaskResult)
}