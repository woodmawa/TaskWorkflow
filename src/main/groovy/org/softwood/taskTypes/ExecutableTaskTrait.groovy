package org.softwood.taskTypes

import java.util.concurrent.CompletableFuture

trait ExecutableTaskTrait<R> extends TaskTrait {
    CompletableFuture<R> execute () {}
    CompletableFuture<R> execute (Map inputVariables) {}
}