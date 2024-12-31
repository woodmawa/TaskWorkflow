package org.softwood.graph

import org.softwood.graph.*

TaskGraph graph = buildTaskGraph {
    // Create start vertex
    vertex("start", StartTask) {
        condition("init", { true })
        connectsTo("process")
    }

    // Create processing vertex with multiple conditions
    vertex("process", ProcessTask) {
        conditions([
                "dataAvailable": { it.hasData() },
                "validFormat": { it.isValid() }
        ])
        connectsTo("validate")
    }

    // Create validation vertex that connects to multiple targets
    vertex("validate", ValidateTask) {
        connectsTo(["success", "error"])
    }

    // Create success and error vertices
    vertex("success", SuccessTask)
    vertex("error", ErrorTask)
}

// Extension method to make the syntax more fluid
def buildTaskGraph(Closure closure) {
    TaskGraphBuilder.build(closure)
}

// Save to JSON file
graph.saveToFile("taskgraph.json")

// Later, load the graph from JSON
def loadedGraph = TaskGraphBuilder.loadFromFile("taskgraph.json")