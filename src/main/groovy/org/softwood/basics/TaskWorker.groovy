package org.softwood.basics

import groovy.util.logging.Slf4j
import org.softwood.taskTypes.Task

@Slf4j
class TaskWorker implements Runnable  {
    private final TaskQueue queue
    private volatile boolean running = true

    TaskWorker(TaskQueue queue) {
        this.queue = queue
    }

    void run() {
        while (running) {
            Task task
            try {
                task = queue.getNextTask()
                if (task) {
                    task.execute()
                }
            } catch (Exception e) {
                log.error "Error processing task ${task.taskName}: ${e.message}"
            }
        }
    }

    void stop() {
        running = false
    }
}
