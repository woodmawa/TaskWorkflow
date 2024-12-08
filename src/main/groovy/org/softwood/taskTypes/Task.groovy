package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

interface Task<T> {
    CompletableFuture<T> execute ()
    CompletableFuture<T> execute (Map inputVariables)
    Map getTaskVariables()
    String getTaskType()
}