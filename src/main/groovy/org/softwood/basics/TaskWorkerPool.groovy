package org.softwood.basics

class TaskWorkerPool {
    private TaskQueue queue
    private List<Thread> workers = []
    private final Object poolLock = new Object()

    TaskPool(TaskQueue taskQueue, int initialSize) {
        queue = taskQueue
        resizePool(initialSize)
    }

    synchronized void resizePool(int newSize) {
        synchronized(poolLock) {
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