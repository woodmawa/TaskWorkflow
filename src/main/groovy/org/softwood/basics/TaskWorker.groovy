package org.softwood.basics

import org.softwood.taskTypes.Task

class TaskWorker implements Runnable  {
    private final TaskQueue queue
    private volatile boolean running = true

    TaskWorker(TaskQueue queue) {
        this.queue = queue
    }

    void run() {
        while (running) {
            try {
                Task task = queue.getNextTask()
                if (task) {
                    task.execute()
                }
            } catch (Exception e) {
                println "Error processing task: ${e.message}"
            }
        }
    }

    void stop() {
        running = false
    }
}
