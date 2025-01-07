package org.softwood.processEngine

import org.softwood.taskTypes.Task

import java.util.concurrent.ConcurrentLinkedQueue

class TaskHistory {
    private static Queue completedTasks = new ConcurrentLinkedQueue<>()

    static void addCompletedTask (Task task) {
        completedTasks.add(task)
    }

    static List getCompletedTasks () {
        completedTasks.asList().asImmutable()
    }

}
