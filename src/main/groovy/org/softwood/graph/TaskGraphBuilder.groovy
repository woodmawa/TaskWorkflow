package org.softwood.graph

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * The builder provides:

 Fluent syntax for creating vertices and connecting them
 Automatic vertex registration in the TaskGraph
 Support for adding conditions at vertex creation time
 Ability to connect vertices using their names
 Support for connecting to multiple target vertices at once
 Validation of vertex existence when creating connections
 Proper scoping of vertex configuration using closures

 This implementation:

 - Maintains the graph structure internally
 - Caches vertices by name for easy lookup
 - Supports nested definition of vertex properties and connections
 - Handles both simple connections and connections with conditions
 -Provides clear error messages for common mistakes
 */

class TaskGraphBuilder {
    private TaskGraph graph = new TaskGraph()
    private Map<String, Vertex> vertexCache = [:]
    private Vertex currentVertex

    static TaskGraph build(Closure closure) {
        def builder = new TaskGraphBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return builder.graph
    }

    def vertex(String name, Class type, Closure closure = null) {
        def vertex = new Vertex(name, type)
        vertexCache[name] = vertex
        graph.addVertex(vertex)

        if (closure) {
            def prevVertex = currentVertex
            currentVertex = vertex
            closure.delegate = this
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()
            currentVertex = prevVertex
        }

        return vertex
    }

    def conditions(Map<String, Closure> conditions) {
        if (!currentVertex) {
            throw new IllegalStateException("conditions must be called within a vertex block")
        }
        currentVertex.conditionsMap.putAll(conditions)
    }

    def condition(String name, Closure condition) {
        if (!currentVertex) {
            throw new IllegalStateException("condition must be called within a vertex block")
        }
        currentVertex.conditionsMap[name] = condition
    }

    def connectsTo(String targetName, Map conditions = null) {
        if (!currentVertex) {
            throw new IllegalStateException("connectsTo must be called within a vertex block")
        }

        def targetVertex = vertexCache[targetName]
        if (!targetVertex) {
            throw new IllegalArgumentException("Target vertex '${targetName}' not found")
        }

        if (conditions) {
            graph.addEdgeWithCondition(currentVertex, targetVertex, conditions)
        } else {
            graph.addEdge(currentVertex, targetVertex)
        }
    }

    def connectsTo(List<String> targetNames, Map conditions = null) {
        targetNames.each { targetName ->
            connectsTo(targetName, conditions)
        }
    }

    // JSON Serialization Methods

    String toJson() {
        def graphData = [
                vertices: vertexCache.collect { name, vertex ->
                    [
                            name: vertex.name,
                            type: vertex.type.name,
                            conditions: serializeConditions(vertex.conditionsMap),
                            connections: graph.getToVertices(vertex)?.collect { it.name } ?: []
                    ]
                }
        ]

        new JsonBuilder(graphData).toPrettyString()
    }

    private Map serializeConditions(Map<String, Closure> conditions) {
        conditions.collectEntries { name, closure ->
            [(name): closure.dehydrate().toString()]
        }
    }

    void saveToFile(String filePath) {
        new File(filePath).text = toJson()
    }

    // JSON Deserialization Methods

    static TaskGraph fromJson(String json) {
        def slurper = new JsonSlurper()
        def graphData = slurper.parseText(json)

        def builder = new TaskGraphBuilder()

        // First pass: Create all vertices
        graphData.vertices.each { vertexData ->
            builder.vertex(
                    vertexData.name,
                    Class.forName(vertexData.type)
            )
        }

        // Second pass: Add conditions and connections
        graphData.vertices.each { vertexData ->
            builder.vertex(vertexData.name, Class.forName(vertexData.type)) {
                // Add conditions
                vertexData.conditions.each { condName, condStr ->
                    condition(condName, deserializeClosure(condStr))
                }

                // Add connections
                if (vertexData.connections) {
                    connectsTo(vertexData.connections as List)
                }
            }
        }

        return builder.graph
    }

    private static Closure deserializeClosure(String closureStr) {
        // This is a simplified implementation - in production you'd want more robust closure deserialization
        Eval.me("""return ${closureStr}""")
    }

    static TaskGraph loadFromFile(String filePath) {
        fromJson(new File(filePath).text)
    }
}


