package org.softwood.processEngine

import java.util.concurrent.ConcurrentLinkedQueue

class ProcessHistory {
    private static Queue<ProcessInstance> completedProcesses = new ConcurrentLinkedQueue<>()

    static void addCompletedProcess (ProcessInstance pi) {
        completedProcesses.add(pi)
    }

    static List getCompletedProcesses () {
        completedProcesses.asList().asImmutable()
    }
}
