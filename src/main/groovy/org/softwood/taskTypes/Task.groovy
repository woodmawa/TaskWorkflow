package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

interface Task<T, R> {
    CompletableFuture<R> execute ()
    CompletableFuture<R> execute (Map inputVariables)
    void setTaskVariables (Map vars)
    Map<String, ? extends Object> getTaskVariables()
    void setTaskType(String name)
    String getTaskType()
    void setTaskName(String name)
    String getTaskName ()
    void setPreviousTaskResults (Task task, CompletableFuture result)
}