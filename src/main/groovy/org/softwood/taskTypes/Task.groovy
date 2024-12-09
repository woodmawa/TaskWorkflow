package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

interface Task<T, R> {
    CompletableFuture<R> execute ()
    CompletableFuture<R> execute (Map inputVariables)
    Map<String, ? extends Object> getTaskVariables()
    String getTaskType()
}