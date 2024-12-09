package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

interface Task<T> {
    CompletableFuture execute ()
    CompletableFuture execute (Map inputVariables)
    Map getTaskVariables()
    String getTaskType()
}