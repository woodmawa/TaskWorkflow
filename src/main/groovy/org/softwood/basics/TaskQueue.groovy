package org.softwood.basics

import org.softwood.taskTypes.Task

import java.util.concurrent.LinkedBlockingQueue

/**
 * thread safe queue of tasks to be processed
 */
class TaskQueue {

    private final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>()

    void addTask(Task task) {
        queue.offer(task)
    }

    Task getNextTask() {
        queue.poll(1, TimeUnit.SECONDS)
    }

    int size() {
        queue.size()
    }
}