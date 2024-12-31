package org.softwood.basics

import groovy.util.logging.Slf4j

@Slf4j
class TaskWorkerPool {
    private TaskQueue queue
    private List<Thread> workers = []
    private final Object poolLock = new Object()

    TaskWorkerPool(TaskQueue taskQueue, int initialSize) {
        queue = taskQueue
        resizePool(initialSize)
    }

    synchronized void resizePool(int newSize) {
        synchronized(poolLock) {
            log.info "resizing task worker pool to $newSize"
            if (newSize > workers.size()) {
                addWorkers(newSize - workers.size())
            } else if (newSize < workers.size()) {
                removeWorkers(workers.size() - newSize)
            }
        }
    }

    private void addWorkers(int count) {
        count.times {
            TaskWorker worker = new TaskWorker(queue)
            Thread thread = new Thread(worker)
            thread.start()
            workers << thread
        }
    }

    private void removeWorkers(int count) {
        count.times {
            if (!workers.isEmpty()) {
                Thread worker = workers.pop()
                worker.interrupt()
            }
        }
    }
}