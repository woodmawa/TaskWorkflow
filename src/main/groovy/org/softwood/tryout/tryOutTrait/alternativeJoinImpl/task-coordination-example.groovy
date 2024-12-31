package org.softwood.tryout.tryOutTrait.alternativeJoinImpl

import org.softwood.graph.TaskGraph
import org.softwood.graph.Vertex

enum TaskStatus {
    NOT_STARTED,
    RUNNING,
    COMPLETED,
    FAILED
}

class TaskVertex extends Vertex {
    TaskStatus status = TaskStatus.NOT_STARTED
    def result
    Set<String> requiredPredecessors = []

    TaskVertex(String name, Class type) {
        super(name, type)
    }

    boolean isReadyToExecute() {
        // For join nodes, check all required predecessors are completed
        if (type == JoinTask) {
            return requiredPredecessors.every { predecessorName ->
                def predecessor = graph.lookupVertexByTaskName(predecessorName)
                predecessor?.status == TaskStatus.COMPLETED
            }
        }
        // For regular nodes, they're ready when not started
        return status == TaskStatus.NOT_STARTED
    }
}

class TaskGraphExecutor {
    private TaskGraph graph
    private Map<String, TaskVertex> vertexCache

    TaskGraphExecutor(TaskGraph graph) {
        this.graph = graph
        this.vertexCache = [:]
    }

    void execute() {
        // Start with the head node
        def currentVertex = graph.head
        processVertex(currentVertex)
    }

    private void processVertex(TaskVertex vertex) {
        if (!vertex.isReadyToExecute()) {
            return
        }

        vertex.status = TaskStatus.RUNNING

        try {
            // Execute the task
            def result = executeTask(vertex)
            vertex.result = result
            vertex.status = TaskStatus.COMPLETED

            // Process successors
            def successors = graph.getToVertices(vertex)
            successors.each { successor ->
                // Update predecessor tracking for join nodes
                if (successor.type == JoinTask) {
                    successor.requiredPredecessors << vertex.name
                }
                processVertex(successor)
            }
        } catch (Exception e) {
            vertex.status = TaskStatus.FAILED
            throw e
        }
    }

    private def executeTask(TaskVertex vertex) {
        // Execute task based on type and conditions
        switch(vertex.type) {
            case ForkTask:
                // Fork task - trigger all successors in parallel
                def successors = graph.getToVertices(vertex)
                return successors.collectParallel { successor ->
                    processVertex(successor)
                }

            case JoinTask:
                // Join task - combine results from predecessors
                def predecessors = graph.getFromVertices(vertex)
                return predecessors.collect { it.result }

            default:
                // Regular task execution
                return vertex.conditionsMap.every { name, condition ->
                    condition.call(vertex)
                }
        }
    }
}

// Enhanced builder methods for fork/join
class TaskGraphBuilder {
    def fork(String name, Closure closure) {
        vertex(name, ForkTask, closure)
    }

    def join(String name, List<String> requiredPredecessors, Closure closure = null) {
        vertex(name, JoinTask) {
            currentVertex.requiredPredecessors.addAll(requiredPredecessors)
            if (closure) {
                closure.delegate = this
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure()
            }
        }
    }
}