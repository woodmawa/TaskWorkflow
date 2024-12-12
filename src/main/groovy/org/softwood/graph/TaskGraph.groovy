package org.softwood.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.softwood.taskTypes.StartTask

class TaskGraph {
    private Map<Vertex, List<Vertex>> adjVertices = [:]
    private Map<Vertex, List<Vertex>> fromVertices = [:]
    private Map<Vertex, List<Vertex>> toVertices = [:]
    private Vertex head

    Vertex getHead () {
        head
    }

    Vertex addVertex(String label, Class type) {
        Vertex vertex = new Vertex(label, type)
        if (!head && type == StartTask)
            head = vertex
        adjVertices.putIfAbsent(vertex, new ArrayList<>())
        fromVertices.putIfAbsent(vertex, new ArrayList<>())
        toVertices.putIfAbsent(vertex, new ArrayList<>())
        vertex
    }

    Vertex addVertex(Vertex v) {
        if (!head && v.type == StartTask)
            head = v
        adjVertices.putIfAbsent(v, new ArrayList<>())
        fromVertices.putIfAbsent(v, new ArrayList<>())
        toVertices.putIfAbsent(v, new ArrayList<>())
    }

    void removeVertex(String label, Class type) {
        Vertex v = new Vertex(label, type)
        adjVertices.values().stream().forEach(e -> e.remove(v))
        adjVertices.remove(v)
    }

    void removeVertex(Vertex v) {
        adjVertices.values().stream().forEach(e -> e.remove(v))
        adjVertices.remove(v)
    }

    /*void addEdge(String label1, String label2) {
        Vertex v1 = new Vertex(label1)
        Vertex v2 = new Vertex(label2)
        adjVertices.get(v1).add(v2)
        adjVertices.get(v2).add(v1)
        fromVertices.get(v2).add(v1)
        toVertices.get(v1).add(v2)
    }*/

    void addEdge(Vertex v1, Vertex v2) {
        adjVertices.get(v1).add(v2)
        adjVertices.get(v2).add(v1)
        fromVertices.get(v2).add(v1)
        toVertices.get(v1).add(v2)
    }

    void removeEdge(String label1, String label2) {
        Vertex v1 = lookupVertexByTaskName (label1)
        Vertex v2 = lookupVertexByTaskName (label1)
        removeEdge (v1, v2)
    }

    void removeEdge(Vertex v1, Vertex v2) {
        List<Vertex> eV1 = adjVertices.get(v1)
        List<Vertex> eV2 = adjVertices.get(v2)
        if (eV1 != null)
            eV1.remove(v2)
        if (eV2 != null)
            eV2.remove(v1)
    }


    List<Vertex> getAdjVertices(String label) {
        def vertex = lookupVertexByTaskName (label)
        return adjVertices.get(vertex)
    }

    List<Vertex> getAdjVertices (Vertex v) {
        return adjVertices.get(v)
    }

    List<Vertex> getFromVertices(String label) {
        def vertex = lookupVertexByTaskName (label)
        return getFromVertices(vertex)
    }

    List<Vertex> getFromVertices(Vertex v) {
        return fromVertices.get(v)
    }


    List<Vertex> getToVertices(String label) {
        def vertex = lookupVertexByTaskName (label)
        return getToVertices(vertex)
    }

    List<Vertex> getToVertices(Vertex v) {
        return toVertices.get(v)
    }

    static Set<Vertex> depthFirstTraversal(TaskGraph graph, Vertex root) {
        Set<Vertex> visited = new LinkedHashSet<Vertex>()
        Stack<Vertex> stack = new Stack<Vertex>()
        stack.push(root)
        while (!stack.isEmpty()) {
            Vertex vertex = stack.pop()
            if (!visited.contains(vertex)) {
                visited.add(vertex)
                def list= graph.getAdjVertices(vertex)
                for (Vertex v : list) {
                    stack.push(v)
                }
            }
        }
        return visited
    }

    static Set<String> breadthFirstTraversal(TaskGraph graph, String root) {
        Set<String> visited = new LinkedHashSet<String>()
        Queue<String> queue = new LinkedList<String>()
        queue.add(root)
        visited.add(root)
        while (!queue.isEmpty()) {
            //get and remove the head of the queue
            String vertex = queue.poll()
            for (Vertex v : graph.getAdjVertices(vertex)) {
                if (!visited.contains(v.name)) {
                    visited.add(v.name)
                    queue.add(v.name)
                }
            }
        }
        return visited
    }

    //look for a task with this name in the task graph by checking the adjVertices map for an entry  and return that vertex
    private lookupVertexByTaskName (String name) {
        def matchedVertex = adjVertices.keySet().find {it.name == name}
        matchedVertex
    }

}

